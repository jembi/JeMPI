package org.jembi.jempi.linker.backend;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.vavr.control.Either;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.stats.StatsTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.jembi.jempi.shared.models.InteractionEnvelop.ContentType.BATCH_END_SENTINEL;
import static org.jembi.jempi.shared.models.InteractionEnvelop.ContentType.BATCH_START_SENTINEL;


public final class BackEnd extends AbstractBehavior<BackEnd.Request> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
   static MyKafkaProducer<String, Notification> topicNotifications;
   private final Executor ec;
   private LibMPI libMPI = null;

   private BackEnd(final ActorContext<Request> context) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      ec = context.getSystem().dispatchers().lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
      if (libMPI == null) {
         openMPI(true);
      }
      topicNotifications = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                 GlobalConstants.TOPIC_NOTIFICATIONS,
                                                 new StringSerializer(),
                                                 new JsonPojoSerializer<>(),
                                                 AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);
   }

   private BackEnd(
         final ActorContext<Request> context,
         final LibMPI lib) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      Configurator.setLevel(LinkerUtils.class, AppConfig.GET_LOG_LEVEL);

      ec = context.getSystem().dispatchers().lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
      libMPI = lib;
   }

   public static Behavior<Request> create() {
      return Behaviors.setup(BackEnd::new);
   }

   public static Behavior<Request> create(final LibMPI lib) {
      return Behaviors.setup(context -> new BackEnd(context, lib));
   }

//   private static float calcNormalizedScore(
//         final CustomDemographicData goldenRecord,
//         final CustomDemographicData interaction) {
//      if (CustomLinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
//         return 1.0F;
//      }
//      return CustomLinkerProbabilistic.probabilisticScore(goldenRecord, interaction);
//   }

   private void openMPI(final boolean useDGraph) {
      if (useDGraph) {
         final var host = AppConfig.getDGraphHosts();
         final var port = AppConfig.getDGraphPorts();
         libMPI = new LibMPI(AppConfig.GET_LOG_LEVEL,
                             host,
                             port,
                             AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                             "CLIENT_ID_LINKER-" + UUID.randomUUID());
      } else {
         libMPI = null;
//         new LibMPI(String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", AppConfig.POSTGRESQL_IP, AppConfig
//         .POSTGRESQL_PORT, AppConfig.POSTGRESQL_DATABASE),
//                             AppConfig.POSTGRESQL_USER,
//                             AppConfig.POSTGRESQL_PASSWORD,
//                             AppConfig.KAFKA_BOOTSTRAP_SERVERS,
//                             "CLIENT_ID_LINKER-" + UUID.randomUUID());
      }
      libMPI.startTransaction();
   }

   @Override
   public Receive<Request> createReceive() {
      return newReceiveBuilder().onMessage(AsyncLinkInteractionRequest.class, this::asyncLinkInteractionHandler)
                                .onMessage(SyncLinkInteractionRequest.class, this::syncLinkInteractionHandler)
//                                .onMessage(SyncLinkInteractionToGidRequest.class, this::syncLinkInteractionToGidHandler)
                                .onMessage(CalculateScoresRequest.class, this::calculateScoresHandler)
                                .onMessage(TeaTimeRequest.class, this::teaTimeHandler)
                                .onMessage(WorkTimeRequest.class, this::workTimeHandler)
                                .onMessage(EventUpdateMUReq.class, this::eventUpdateMUReqHandler)
//                              .onMessage(EventGetMUReq.class, this::eventGetMUReqHandler)
                                .onMessage(CrCandidatesRequest.class, this::crCandidates)
                                .onMessage(CrFindRequest.class, this::crFind)
                                .onMessage(CrRegisterRequest.class, this::crRegister)
                                .onMessage(CrUpdateFieldRequest.class, this::crUpdateField)
                                .onMessage(RunStartStopHooksRequest.class, this::runStartStopHooks)
                                .build();
   }

   private Behavior<Request> crCandidates(final CrCandidatesRequest req) {
      final var matchedCandidates = LinkerCR.crCandidates(libMPI, req.crCandidatesData);
      if (matchedCandidates.isEmpty()) {
         req.replyTo.tell(new CrCandidatesResponse(Either.right(List.of())));
      } else {
         req.replyTo.tell(new CrCandidatesResponse(Either.right(matchedCandidates)));
      }
      return Behaviors.same();
   }

   private Behavior<Request> crFind(final CrFindRequest req) {
      final var goldenRecords = LinkerCR.crFind(libMPI, req.crFindData);
      req.replyTo.tell(new CrFindResponse(Either.right(goldenRecords)));
      return Behaviors.same();
   }

   private Behavior<Request> crUpdateField(final CrUpdateFieldRequest req) {
      final var result = LinkerCR.crUpdateField(libMPI, req.crUpdateFields);
      req.replyTo.tell(new CrUpdateFieldResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> runStartStopHooks(final RunStartStopHooksRequest req) {
      List<MpiGeneralError> hookRunErrors = List.of();

      if (req.batchInteraction.contentType() == BATCH_START_SENTINEL) {
         hookRunErrors = libMPI.beforeLinkingHook();
      } else if (req.batchInteraction.contentType() == BATCH_END_SENTINEL) {
         hookRunErrors = libMPI.afterLinkingHook();
      }
      req.replyTo.tell(new RunStartStopHooksResponse(hookRunErrors));
      return Behaviors.same();
   }

   private Behavior<Request> crRegister(final CrRegisterRequest req) {
      final var result = LinkerCR.crRegister(libMPI, req.crRegister);
      req.replyTo.tell(new CrRegisterResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> syncLinkInteractionHandler(final SyncLinkInteractionRequest request) {
      final var listLinkInfo = LinkerDWH.linkInteraction(libMPI,
                                                         new Interaction(null,
                                                                         request.link.sourceId(),
                                                                         request.link.uniqueInteractionData(),
                                                                         request.link.demographicData()),
                                                         request.link.externalLinkRange(),
                                                         request.link.matchThreshold() == null
                                                               ? AppConfig.LINKER_MATCH_THRESHOLD
                                                               : request.link.matchThreshold(),
                                                         request.link.stan());
      request.replyTo.tell(new SyncLinkInteractionResponse(request.link.stan(),
                                                           listLinkInfo.isLeft()
                                                                 ? listLinkInfo.getLeft()
                                                                 : null,
                                                           listLinkInfo.isRight()
                                                                 ? listLinkInfo.get()
                                                                 : null));
      return Behaviors.same();
   }

   private Behavior<Request> asyncLinkInteractionHandler(final AsyncLinkInteractionRequest req) {
      if (req.batchInteraction.contentType() != InteractionEnvelop.ContentType.BATCH_INTERACTION) {
         return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, TeaTimeRequest.INSTANCE, Duration.ofSeconds(5));
            req.replyTo.tell(new AsyncLinkInteractionResponse(null));
            return Behaviors.same();
         });
      }
      final var linkInfo =
            LinkerDWH.linkInteraction(libMPI,
                                      req.batchInteraction.interaction(),
                                      null,
                                      AppConfig.LINKER_MATCH_THRESHOLD,
                                      req.batchInteraction.stan());
      if (linkInfo.isLeft()) {
         req.replyTo.tell(new AsyncLinkInteractionResponse(linkInfo.getLeft()));
      } else {
         req.replyTo.tell(new AsyncLinkInteractionResponse(null));
      }
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, TeaTimeRequest.INSTANCE, Duration.ofSeconds(10));
         return Behaviors.same();
      });
   }

   private Behavior<Request> workTimeHandler(final WorkTimeRequest request) {
      LOGGER.info("WORK TIME");
      return Behaviors.same();
   }

   private Behavior<Request> teaTimeHandler(final TeaTimeRequest request) {
      if (LOGGER.isInfoEnabled()) {
         LOGGER.info("TEA TIME");
      }
      var cf = CompletableFuture.supplyAsync(() -> {
         if (LOGGER.isInfoEnabled()) {
            LOGGER.info("START STATS");
         }
         final var statsTask = new StatsTask();
         var rc = statsTask.run();
         if (LOGGER.isInfoEnabled()) {
            LOGGER.info("END STATS: {}", rc);
         }
         return rc;
      }, ec);

      cf.whenComplete((event, exception) -> {
         // POST TO LAB
      });
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, WorkTimeRequest.INSTANCE, Duration.ofSeconds(5));
         return Behaviors.same();
      });
   }

   private Behavior<Request> calculateScoresHandler(final CalculateScoresRequest request) {
      final var interaction = libMPI.findInteraction(request.calculateScoresRequest.interactionId());
      final var goldenRecords = libMPI.findGoldenRecords(request.calculateScoresRequest.goldenIds());
      final var scores = goldenRecords.parallelStream()
                                      .unordered()
                                      .map(goldenRecord -> new ApiModels.ApiCalculateScoresResponse.ApiScore(goldenRecord.goldenId(),
                                                                                                             LinkerUtils.calcNormalizedLinkScore(
                                                                                                                   goldenRecord.demographicData(),
                                                                                                                   interaction.demographicData())))
                                      .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                      .collect(Collectors.toCollection(ArrayList::new));
      request.replyTo.tell(new CalculateScoresResponse(new ApiModels.ApiCalculateScoresResponse(request.calculateScoresRequest.interactionId(),
                                                                                                scores)));
      return Behaviors.same();
   }


   private Behavior<Request> eventUpdateMUReqHandler(final EventUpdateMUReq req) {
      LinkerProbabilistic.updateMU(req.mu);
      req.replyTo.tell(new EventUpdateMURsp(true));
      return Behaviors.same();
   }


//   private Behavior<Request> eventGetMUReqHandler(final EventGetMUReq req) {
//      req.replyTo.tell(new EventGetMURsp(CustomLinkerProbabilistic.getMU()));
//      return Behaviors.same();
//   }

   private enum TeaTimeRequest implements Request {
      INSTANCE
   }

   private enum WorkTimeRequest implements Request {
      INSTANCE
   }

   public interface Request {
   }

   public interface Response {
   }

   private record WorkCandidate(
         GoldenRecord goldenRecord,
         float score) {
   }

   public record AsyncLinkInteractionRequest(
         ActorRef<AsyncLinkInteractionResponse> replyTo,
         String key,
         InteractionEnvelop batchInteraction) implements Request {
   }

   public record AsyncLinkInteractionResponse(LinkInfo linkInfo) implements Response {
   }

   public record RunStartStopHooksRequest(
         ActorRef<RunStartStopHooksResponse> replyTo,
         String key,
         InteractionEnvelop batchInteraction) implements Request {
   }

   public record RunStartStopHooksResponse(List<MpiGeneralError> hooksResults) implements Response {
   }

   public record EventUpdateMUReq(
         CustomMU mu,
         ActorRef<EventUpdateMURsp> replyTo) implements Request {
   }

   public record EventUpdateMURsp(boolean rc) implements Response {
   }

//   public record EventGetMUReq(ActorRef<EventGetMURsp> replyTo) implements Request {
//   }
//
//   public record EventGetMURsp(CustomMU mu) implements Response {
//   }

   public record CalculateScoresRequest(
         ApiModels.ApiCalculateScoresRequest calculateScoresRequest,
         ActorRef<CalculateScoresResponse> replyTo) implements Request {

   }

   public record CalculateScoresResponse(
         ApiModels.ApiCalculateScoresResponse calculateScoresResponse) {

   }

   public record SyncLinkInteractionRequest(
         ApiModels.LinkInteractionSyncBody link,
         ActorRef<SyncLinkInteractionResponse> replyTo) implements Request {
   }

   public record SyncLinkInteractionResponse(
         String stan,
         LinkInfo linkInfo,
         List<ExternalLinkCandidate> externalLinkCandidateList) implements Response {
   }

   public record SyncLinkInteractionToGidRequest(
         ApiModels.LinkInteractionToGidSyncBody link,
         ActorRef<SyncLinkInteractionToGidResponse> replyTo) implements Request {
   }

   public record SyncLinkInteractionToGidResponse(
         String stan,
         LinkInfo linkInfo) implements Response {
   }

   public record FindCandidatesWithScoreRequest(
         ActorRef<FindCandidatesWithScoreResponse> replyTo,
         String iid) implements Request {
   }

   public record FindCandidatesWithScoreResponse(Either<MpiGeneralError, List<Candidate>> candidates) implements Response {
      public record Candidate(
            GoldenRecord goldenRecord,
            float score) {
      }
   }

   public record CrRegisterRequest(
         ApiModels.ApiCrRegisterRequest crRegister,
         ActorRef<CrRegisterResponse> replyTo) implements Request {
   }

   public record CrRegisterResponse(
         Either<MpiGeneralError, LinkInfo> linkInfo) implements Response {
   }

   public record CrCandidatesRequest(
         ApiModels.ApiCrCandidatesRequest crCandidatesData,
         ActorRef<CrCandidatesResponse> replyTo) implements Request {
   }

   public record CrCandidatesResponse(
         Either<MpiGeneralError, List<GoldenRecord>> goldenRecords) implements Response {
   }

   public record CrFindRequest(
         ApiModels.ApiCrFindRequest crFindData,
         ActorRef<CrFindResponse> replyTo) implements Request {
   }

   public record CrFindResponse(
         Either<MpiGeneralError, List<GoldenRecord>> goldenRecords) implements Response {
   }

   public record CrUpdateFieldRequest(
         ApiModels.ApiCrUpdateFieldsRequest crUpdateFields,
         ActorRef<CrUpdateFieldResponse> replyTo) implements Request {
   }

   public record CrUpdateFieldResponse(
         Either<MpiGeneralError, UpdateFieldResponse> response) implements Response {
      public record UpdateFieldResponse(
            String goldenId,
            List<String> updated,
            List<String> failed) {
      }
   }


}
