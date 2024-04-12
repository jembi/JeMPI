package org.jembi.jempi.linker.backend;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
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

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;


public final class BackEnd extends AbstractBehavior<BackEnd.Request> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
   static MyKafkaProducer<String, Notification> topicNotifications;

   static {
      topicNotifications = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                 GlobalConstants.TOPIC_NOTIFICATIONS,
                                                 new StringSerializer(),
                                                 new JsonPojoSerializer<>(),
                                                 AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);
   }

   private final Executor ec;
   private LibMPI libMPI = null;

   private BackEnd(final ActorContext<Request> context) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      ec = context.getSystem().dispatchers().lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
      if (libMPI == null) {
         openMPI();
      }
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

   private void openMPI() {
      final var host = AppConfig.getDGraphHosts();
      final var port = AppConfig.getDGraphPorts();
      libMPI = new LibMPI(AppConfig.GET_LOG_LEVEL,
                          host,
                          port,
                          AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                          "CLIENT_ID_LINKER-" + UUID.randomUUID());
   }

   @Override
   public Receive<Request> createReceive() {
      return newReceiveBuilder().onMessage(AsyncLinkInteractionRequest.class, this::asyncLinkInteractionHandler)
                                .onMessage(SyncLinkInteractionRequest.class, this::syncLinkInteractionHandler)
//                              .onMessage(SyncLinkInteractionToGidRequest.class, this::syncLinkInteractionToGidHandler)
                                .onMessage(CalculateScoresRequest.class, this::calculateScoresHandler)
                                .onMessage(TeaTimeRequest.class, this::teaTimeHandler)
                                .onMessage(WorkTimeRequest.class, this::workTimeHandler)
                                .onMessage(EventUpdateMUReq.class, this::eventUpdateMUReqHandler)
//                              .onMessage(EventGetMUReq.class, this::eventGetMUReqHandler)
                                .onMessage(CrCandidatesRequest.class, this::crCandidates)
                                .onMessage(CrFindRequest.class, this::crFind)
                                .onMessage(CrRegisterRequest.class, this::crRegister)
                                .onMessage(CrLinkToGidUpdateRequest.class, this::crLinkToGidUpdate)
                                .onMessage(CrLinkBySourceIdRequest.class, this::crLinkBySourceId)
                                .onMessage(CrLinkBySourceIdUpdateRequest.class, this::crLinkBySourceIdUpdate)
                                .onMessage(CrUpdateFieldRequest.class, this::crUpdateField)
                                .onMessage(FindCandidatesWithScoreRequest.class, this::findCandidateWithScoreHandler)
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
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(req.crFindData());
         LOGGER.debug("{}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      final var goldenRecords = LinkerCR.crFind(libMPI, req.crFindData);
      if (goldenRecords.isRight()) {
         req.replyTo.tell(new CrFindResponse(Either.right(goldenRecords.get())));
      } else {
         req.replyTo.tell(new CrFindResponse(Either.left(goldenRecords.getLeft())));
      }
      return Behaviors.same();
   }

   private Behavior<Request> crUpdateField(final CrUpdateFieldRequest req) {
      final var result = LinkerCR.crUpdateField(libMPI, req.crUpdateFields);
      req.replyTo.tell(new CrUpdateFieldResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> crRegister(final CrRegisterRequest req) {
      final var result = LinkerCR.crRegister(libMPI, req.crRegister);
      req.replyTo.tell(new CrRegisterResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> crLinkToGidUpdate(final CrLinkToGidUpdateRequest req) {
      final var result = LinkerCR.crLinkToGidUpdate(libMPI, req.crLinkToGidUpdate);
      req.replyTo.tell(new CrLinkToGidUpdateResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> crLinkBySourceId(final CrLinkBySourceIdRequest req) {
      final var result = LinkerCR.crLinkBySourceId(libMPI, req.crLinkBySourceId);
      req.replyTo.tell(new CrLinkBySourceIdResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> crLinkBySourceIdUpdate(final CrLinkBySourceIdUpdateRequest req) {
      final var result = LinkerCR.crLinkBySourceIdUpdate(libMPI, req.crLinkBySourceIdUpdate);
      req.replyTo.tell(new CrLinkBySourceIdUpdateResponse(result));
      return Behaviors.same();
   }

   private Behavior<Request> findCandidateWithScoreHandler(final FindCandidatesWithScoreRequest req) {
      LOGGER.warn("findCandidateWithScoreHandler not implemented");
      final var result = new MpiServiceError.NotImplementedError("findCandidateWithScore not implemented");
      req.replyTo.tell(new FindCandidatesWithScoreResponse(Either.left(result)));
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
                                                           listLinkInfo.isRight()
                                                                 ? listLinkInfo.get()
                                                                 : null,
                                                           listLinkInfo.isLeft()
                                                                 ? listLinkInfo.getLeft()
                                                                 : null));
      return Behaviors.same();
   }

   private Behavior<Request> asyncLinkInteractionHandler(final AsyncLinkInteractionRequest req) {
      if (req.batchInteraction.contentType() != InteractionEnvelop.ContentType.BATCH_INTERACTION) {
         return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY,
                                    TeaTimeRequest.INSTANCE,
                                    Duration.ofSeconds(GlobalConstants.TIMEOUT_TEA_TIME_SECS));
            req.replyTo.tell(new AsyncLinkInteractionResponse(null));
            return Behaviors.same();
         });
      }
      final var linkInfo =
            LinkerDWH.linkInteraction(libMPI,
                                      req.batchInteraction.interaction(),
                                      null,
                                      req.batchInteraction.config() != null
                                            ? req.batchInteraction.config().linkThreshold().floatValue()
                                            : AppConfig.LINKER_MATCH_THRESHOLD,
                                      req.batchInteraction.stan());
      if (linkInfo.isRight()) {
         req.replyTo.tell(new AsyncLinkInteractionResponse(linkInfo.get()));
      } else {
         req.replyTo.tell(new AsyncLinkInteractionResponse(null));
      }
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY,
                                 TeaTimeRequest.INSTANCE,
                                 Duration.ofSeconds(GlobalConstants.TIMEOUT_TEA_TIME_SECS));
         return Behaviors.same();
      });
   }

/*
   private Behavior<Request> syncLinkInteractionToGidHandler(final SyncLinkInteractionToGidRequest request) {
      final LinkInfo linkInfo;
      final var interaction = request.link.interaction();
      final var gid = request.link.gid();
      try {
         // Check if we have new M&U values
         LinkerProbabilistic.checkUpdatedMU();

         libMPI.startTransaction();
         if (StringUtils.isBlank(gid)) {
            linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
         } else {
            final var goldenRecord = libMPI.findGoldenRecord(gid);
            if (goldenRecord == null) {
               LOGGER.error("Golden Record for GID {} is null", gid);
               linkInfo = null;
            } else {
               final var validated1 = CustomLinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(),
                                                                                           interaction.demographicData());
               final var validated2 = CustomLinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(),
                                                                                           interaction.demographicData());

               linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction,
                                                                                new LibMPIClientInterface.GoldenIdScore(gid,
                                                                                                                        3.0F),
                                                                                validated1, validated2);
               if (Boolean.TRUE.equals(goldenRecord.customUniqueGoldenRecordData().auxAutoUpdateEnabled())) {
                  CustomLinkerBackEnd.updateGoldenRecordFields(libMPI, 0.0F, linkInfo.interactionUID(), gid);
               }
            }
         }
      } finally {
         libMPI.closeTransaction();
      }
      request.replyTo.tell(new SyncLinkInteractionToGidResponse(request.link.stan(), linkInfo));
      return Behaviors.same();
   }
*/

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
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY,
                                 WorkTimeRequest.INSTANCE,
                                 Duration.ofSeconds(GlobalConstants.TIMEOUT_TEA_TIME_SECS));
         return Behaviors.same();
      });
   }

   private Behavior<Request> calculateScoresHandler(final CalculateScoresRequest request) {
      final var interaction = libMPI.findInteraction(request.calculateScoresRequest.interactionId());
      final var goldenRecords = libMPI.findGoldenRecords(request.calculateScoresRequest.goldenIds());
      if (goldenRecords.isRight()) {
         final var scores = goldenRecords.get().parallelStream()
                                         .unordered()
                                         .map(goldenRecord -> new ApiModels.ApiCalculateScoresResponse.ApiScore(goldenRecord.goldenId(),
                                                                                                                LinkerUtils.calcNormalizedScore(
                                                                                                                      goldenRecord.demographicData(),
                                                                                                                      interaction.demographicData())))
                                         .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                         .collect(Collectors.toCollection(ArrayList::new));
         request.replyTo.tell(new CalculateScoresResponse(new ApiModels.ApiCalculateScoresResponse(request.calculateScoresRequest.interactionId(),
                                                                                                   scores)));
      } else {
         request.replyTo.tell(new CalculateScoresResponse(new ApiModels.ApiCalculateScoresResponse(null, List.of())));
      }
      return Behaviors.same();
   }


   private Behavior<Request> eventUpdateMUReqHandler(final EventUpdateMUReq req) {
      CustomLinkerProbabilistic.updateMU(req.mu);
      req.replyTo.tell(new EventUpdateMURsp(true));
      return Behaviors.same();
   }

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

   public record AsyncLinkInteractionRequest(
         ActorRef<AsyncLinkInteractionResponse> replyTo,
         String key,
         InteractionEnvelop batchInteraction) implements Request {
   }

   public record AsyncLinkInteractionResponse(LinkInfo linkInfo) implements Response {
   }

   public record EventUpdateMUReq(
         CustomMU mu,
         ActorRef<EventUpdateMURsp> replyTo) implements Request {
   }

   public record EventUpdateMURsp(boolean rc) implements Response {
   }

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

   public record CrLinkToGidUpdateRequest(
         ApiModels.ApiCrLinkToGidUpdateRequest crLinkToGidUpdate,
         ActorRef<CrLinkToGidUpdateResponse> replyTo) implements Request {
   }

   public record CrLinkToGidUpdateResponse(
         Either<MpiGeneralError, LinkInfo> linkInfo) implements Response {
   }

   public record CrLinkBySourceIdRequest(
         ApiModels.ApiCrLinkBySourceIdRequest crLinkBySourceId,
         ActorRef<CrLinkBySourceIdResponse> replyTo) implements Request {
   }

   public record CrLinkBySourceIdResponse(
         Either<MpiGeneralError, LinkInfo> linkInfo) implements Response {
   }

   public record CrLinkBySourceIdUpdateRequest(
         ApiModels.ApiCrLinkBySourceIdUpdateRequest crLinkBySourceIdUpdate,
         ActorRef<CrLinkBySourceIdUpdateResponse> replyTo) implements Request {
   }

   public record CrLinkBySourceIdUpdateResponse(
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
