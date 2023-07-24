package org.jembi.jempi.linker.backend;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;
import org.jembi.jempi.stats.StatsTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public final class BackEnd extends AbstractBehavior<BackEnd.Request> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
   private final Executor ec;
   private LibMPI libMPI = null;
   private MyKafkaProducer<String, Notification> topicNotifications;
   private MyKafkaProducer<String, AuditEvent> topicAuditEvents;

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
      topicAuditEvents = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                               GlobalConstants.TOPIC_AUDIT_TRAIL,
                                               new StringSerializer(),
                                               new JsonPojoSerializer<>(),
                                               AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);
   }

   private BackEnd(
         final ActorContext<Request> context,
         final LibMPI lib) {
      super(context);
      Configurator.setLevel(this.getClass(), AppConfig.GET_LOG_LEVEL);
      ec = context.getSystem().dispatchers().lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));
      libMPI = lib;
   }

   public static Behavior<Request> create() {
      return Behaviors.setup(BackEnd::new);
   }

   public static Behavior<Request> create(final LibMPI lib) {
      return Behaviors.setup(context -> new BackEnd(context, lib));
   }

   private static float calcNormalizedScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      if (CustomLinkerDeterministic.deterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }
      return CustomLinkerProbabilistic.probabilisticScore(goldenRecord, interaction);
   }

   private static boolean isBetterValue(
         final String textLeft,
         final long countLeft,
         final String textRight,
         final long countRight) {
      return (StringUtils.isBlank(textLeft) && countRight >= 1) || (countRight > countLeft && !textRight.equals(textLeft));
   }

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
         libMPI = new LibMPI(String.format("jdbc:postgresql://postgresql:5432/%s", AppConfig.POSTGRESQL_DATABASE),
                             AppConfig.POSTGRESQL_USER,
                             AppConfig.POSTGRESQL_PASSWORD,
                             AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                             "CLIENT_ID_LINKER-" + UUID.randomUUID());
      }
      libMPI.startTransaction();
      if (!(libMPI.dropAll().isEmpty() && libMPI.createSchema().isEmpty())) {
         LOGGER.error("Create Schema Error");
      }
      libMPI.closeTransaction();
   }

   @Override
   public Receive<Request> createReceive() {
      return newReceiveBuilder().onMessage(AsyncLinkInteractionRequest.class, this::asyncLinkInteractionHandler)
                                .onMessage(SyncLinkInteractionRequest.class, this::syncLinkInteractionHandler)
                                .onMessage(SyncLinkInteractionToGidRequest.class, this::syncLinkInteractionToGidHandler)
                                .onMessage(CalculateScoresRequest.class, this::calculateScoresHandler)
                                .onMessage(TeaTimeRequest.class, this::teaTimeHandler)
                                .onMessage(WorkTimeRequest.class, this::workTimeHandler)
                                .onMessage(EventUpdateMUReq.class, this::eventUpdateMUReqHandler)
                                .onMessage(EventGetMUReq.class, this::eventGetMUReqHandler)
                                .onMessage(CrFindRequest.class, this::crFind)
                                .onMessage(CrRegisterRequest.class, this::crRegister)
                                .onMessage(CrUpdateFieldRequest.class, this::crUpdateField)
                                .build();
   }

   private Behavior<Request> crFind(final CrFindRequest req) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", req.crFindData.parameters());
      }
      final var params = req.crFindData.parameters();
      final var rec = libMPI.simpleSearchGoldenRecords(params,
                                                       0,
                                                       100,
                                                       "givenName",
                                                       true);
      if (rec == null) {
         req.replyTo.tell(new CrFindResponse(Either.right(List.of())));
      } else {
         req.replyTo.tell(new CrFindResponse(Either.right(rec.data().stream().map(ExpandedGoldenRecord::goldenRecord).toList())));
      }
      return Behaviors.same();
   }

   private Behavior<Request> crRegister(final CrRegisterRequest req) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", req.crRegister.demographicData());
      }

      final var candidateGoldenRecords = libMPI.findCandidates(req.crRegister.demographicData());
      if (candidateGoldenRecords.isEmpty()) {
         final var interaction = new Interaction(null,
                                                 req.crRegister.sourceId(),
                                                 req.crRegister.uniqueInteractionData(),
                                                 req.crRegister.demographicData());
         final var linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
         req.replyTo.tell(new CrRegisterResponse(Either.right(linkInfo)));
      } else {
         req.replyTo.tell(new CrRegisterResponse(Either.left(new MpiServiceError.CRClientExistsError(candidateGoldenRecords.get(0)
                                                                                                                           .demographicData(),
                                                                                                     req.crRegister.demographicData()))));
      }
      return Behaviors.same();
   }

   private Behavior<Request> crUpdateField(final CrUpdateFieldRequest req) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{} {} {}", req.crUpdateField.goldenId(), req.crUpdateField.field(), req.crUpdateField.value());
      }
      final var success = libMPI.updateGoldenRecordField(req.crUpdateField.goldenId(),
                                                         req.crUpdateField.field(),
                                                         req.crUpdateField.value());
      LOGGER.debug("{}", success);
      if (success) {
         req.replyTo.tell(new CrUpdateFieldResponse(Either.right(new CrUpdateFieldResponse.UpdateFieldResponse(req.crUpdateField.goldenId(),
                                                                                                               req.crUpdateField.field(),
                                                                                                               req.crUpdateField.value()))));
      } else {
         req.replyTo.tell(new CrUpdateFieldResponse(Either.left(new MpiServiceError.NotImplementedError("crUpdateField"))));
      }
      return Behaviors.same();
   }

   private Behavior<Request> asyncLinkInteractionHandler(final AsyncLinkInteractionRequest req) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", req.batchInteraction.stan());
      }
      if (req.batchInteraction.contentType() != InteractionEnvelop.ContentType.BATCH_INTERACTION) {
         return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, TeaTimeRequest.INSTANCE, Duration.ofSeconds(5));
            req.replyTo.tell(new AsyncLinkInteractionResponse(null));
            return Behaviors.same();
         });
      }
      final var listLinkInfo =
            linkInteraction(req.batchInteraction.interaction(), null, AppConfig.LINKER_MATCH_THRESHOLD);
      req.replyTo.tell(new AsyncLinkInteractionResponse(listLinkInfo.getLeft()));
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, TeaTimeRequest.INSTANCE, Duration.ofSeconds(10));
         return Behaviors.same();
      });
   }

   private Behavior<Request> syncLinkInteractionHandler(final SyncLinkInteractionRequest request) {
      final var listLinkInfo =
            linkInteraction(request.link.interaction(), request.link.externalLinkRange(), request.link.matchThreshold());
      request.replyTo.tell(new SyncLinkInteractionResponse(request.link.stan(),
                                                           listLinkInfo.isLeft()
                                                                 ? listLinkInfo.getLeft()
                                                                 : null,
                                                           listLinkInfo.isRight()
                                                                 ? listLinkInfo.get()
                                                                 : null));
      return Behaviors.same();
   }

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
               linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction,
                                                                                new LibMPIClientInterface.GoldenIdScore(gid,
                                                                                                                        3.0F));
               if (Boolean.TRUE.equals(goldenRecord.customUniqueGoldenRecordData().auxAutoUpdateEnabled())) {
                  CustomLinkerBackEnd.updateGoldenRecordFields(this, libMPI, 0.0F, linkInfo.interactionUID(), gid);
               }
            }
         }
      } finally {
         libMPI.closeTransaction();
      }
      request.replyTo.tell(new SyncLinkInteractionToGidResponse(request.link.stan(), linkInfo));
      return Behaviors.same();
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
                                                                                                             calcNormalizedScore(
                                                                                                                   goldenRecord.demographicData(),
                                                                                                                   interaction.demographicData())))
                                      .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                      .collect(Collectors.toCollection(ArrayList::new));
      request.replyTo.tell(
            new CalculateScoresResponse(
                  new ApiModels.ApiCalculateScoresResponse(request.calculateScoresRequest.interactionId(),
                                                           scores)));
      return Behaviors.same();
   }

   private Behavior<Request> eventUpdateMUReqHandler(final EventUpdateMUReq req) {
      CustomLinkerProbabilistic.updateMU(req.mu);
      req.replyTo.tell(new EventUpdateMURsp(true));
      return Behaviors.same();
   }

   private Behavior<Request> eventGetMUReqHandler(final EventGetMUReq req) {
      req.replyTo.tell(new EventGetMURsp(CustomLinkerProbabilistic.getMU()));
      return Behaviors.same();
   }


   private Either<LinkInfo, List<ExternalLinkCandidate>> linkInteraction(
         final Interaction interaction,
         final ExternalLinkRange externalLinkRange,
         final float matchThreshold_) {
      LinkInfo linkInfo = null;
      final List<ExternalLinkCandidate> externalLinkCandidateList = new ArrayList<>();
      final var matchThreshold = externalLinkRange != null
            ? externalLinkRange.high()
            : matchThreshold_;
      try {
         libMPI.startTransaction();
         LinkerProbabilistic.checkUpdatedMU();
         final var candidateGoldenRecords = libMPI.findCandidates(interaction.demographicData());
         if (candidateGoldenRecords.isEmpty()) {
            linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
/*
            sendAuditEvent(linkInfo.interactionUID(), linkInfo.goldenUID(), "Interaction -> New GoldenRecord (1.00000)");
*/
         } else {
            final var allCandidateScores = candidateGoldenRecords.parallelStream()
                                                                 .unordered()
                                                                 .map(candidate -> new WorkCandidate(candidate,
                                                                                                     calcNormalizedScore(candidate.demographicData(),
                                                                                                                         interaction.demographicData())))
                                                                 .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                                                 .collect(Collectors.toCollection(ArrayList::new));

            // Get a list of candidates withing the supplied for external link range
            final var candidatesInExternalLinkRange = externalLinkRange == null
                  ? new ArrayList<WorkCandidate>()
                  : allCandidateScores.stream()
                                      .filter(v -> v.score() >= externalLinkRange.low() && v.score() <= externalLinkRange.high())
                                      .collect(Collectors.toCollection(ArrayList::new));

            // Get a list of candidates above the supplied threshold
            final var belowThresholdNotifications = new ArrayList<Notification.MatchData>();
            final var aboveThresholdNotifications = new ArrayList<Notification.MatchData>();
            final var candidatesAboveMatchThreshold =
                  allCandidateScores
                        .stream()
                        .peek(v -> {
                           if (v.score() > matchThreshold - 0.1 && v.score() < matchThreshold) {
                              belowThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(), v.score()));
                           } else if (v.score() >= matchThreshold && v.score() < matchThreshold + 0.1) {
                              aboveThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(), v.score()));
                           }
                        })
                        .filter(v -> v.score() >= matchThreshold)
                        .collect(Collectors.toCollection(ArrayList::new));

            if (candidatesAboveMatchThreshold.isEmpty()) {
               if (candidatesInExternalLinkRange.isEmpty()) {
                  linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
/*
                  sendAuditEvent(linkInfo.interactionUID(), linkInfo.goldenUID(), "Interaction -> New GoldenRecord (1.00000)");
*/
                  if (!belowThresholdNotifications.isEmpty()) {
                     sendNotification(Notification.NotificationType.THRESHOLD,
                                      linkInfo.interactionUID(),
                                      AppUtils.getNames(interaction.demographicData()),
                                      new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                      belowThresholdNotifications);
                  }
               } else {
                  candidatesInExternalLinkRange.forEach(candidate -> externalLinkCandidateList.add(new ExternalLinkCandidate(
                        candidate.goldenRecord,
                        candidate.score)));
               }
            } else {
               final var firstCandidate = candidatesAboveMatchThreshold.get(0);
               final var linkToGoldenId =
                     new LibMPIClientInterface.GoldenIdScore(firstCandidate.goldenRecord.goldenId(), firstCandidate.score);
               linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction, linkToGoldenId);
/*
               sendAuditEvent(linkInfo.interactionUID(),
                              linkInfo.goldenUID(),
                              String.format("Interaction -> Existing GoldenRecord (%.5f)", linkToGoldenId.score()));
*/
               if (linkToGoldenId.score() <= matchThreshold + 0.1) {
                  sendNotification(Notification.NotificationType.THRESHOLD,
                                   linkInfo.interactionUID(),
                                   AppUtils.getNames(interaction.demographicData()),
                                   new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                   aboveThresholdNotifications);
               }
               if (Boolean.TRUE.equals(firstCandidate.goldenRecord.customUniqueGoldenRecordData().auxAutoUpdateEnabled())) {
                  CustomLinkerBackEnd.updateGoldenRecordFields(this,
                                                               libMPI,
                                                               matchThreshold,
                                                               linkInfo.interactionUID(),
                                                               linkInfo.goldenUID());
               }
               final var marginCandidates = new ArrayList<Notification.MatchData>();
               if (candidatesInExternalLinkRange.isEmpty() && candidatesAboveMatchThreshold.size() > 1) {
                  for (var i = 1; i < candidatesAboveMatchThreshold.size(); i++) {
                     final var candidate = candidatesAboveMatchThreshold.get(i);
                     if (firstCandidate.score - candidate.score <= 0.1) {
                        marginCandidates.add(new Notification.MatchData(candidate.goldenRecord.goldenId(), candidate.score));
                     } else {
                        break;
                     }
                  }
                  if (!marginCandidates.isEmpty()) {
                     sendNotification(Notification.NotificationType.MARGIN,
                                      linkInfo.interactionUID(),
                                      AppUtils.getNames(interaction.demographicData()),
                                      new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                      marginCandidates);
                  }
               }
            }
         }
      } finally {
         libMPI.closeTransaction();
      }
      return linkInfo == null
            ? Either.right(externalLinkCandidateList)
            : Either.left(linkInfo);
   }

   private void sendNotification(
         final Notification.NotificationType type,
         final String dID,
         final String names,
         final Notification.MatchData linkedTo,
         final List<Notification.MatchData> candidates) {
      final var notification = new Notification(System.currentTimeMillis(), type, dID, names, linkedTo, candidates);
      try {
         topicNotifications.produceSync("dummy", notification);
      } catch (ExecutionException | InterruptedException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

   }

/*
   private void sendAuditEvent(
         final String interactionID,
         final String goldenID,
         final String event) {
      topicAuditEvents.produceAsync(goldenID,
                                    new AuditEvent(new Timestamp(System.currentTimeMillis()),
                                                   null,
                                                   interactionID,
                                                   goldenID,
                                                   event),
                                    ((metadata, exception) -> {
                                       if (exception != null) {
                                          LOGGER.error(exception.getLocalizedMessage(), exception);
                                       }
                                    }));

   }

*/

   boolean helperUpdateGoldenRecordField(
         final String interactionId,
         final ExpandedGoldenRecord expandedGoldenRecord,
         final String fieldName,
         final String goldenRecordFieldValue,
         final Function<CustomDemographicData, String> getDemographicField) {

      boolean changed = false;

      if (expandedGoldenRecord == null) {
         LOGGER.error("expandedGoldenRecord cannot be null");
      } else {
         final var mpiInteractions = expandedGoldenRecord.interactionsWithScore();
         final var freqMapGroupedByField = mpiInteractions.stream()
                                                          .map(mpiInteraction -> getDemographicField.apply(mpiInteraction.interaction()
                                                                                                                         .demographicData()))
                                                          .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
         freqMapGroupedByField.remove(StringUtils.EMPTY);
         if (freqMapGroupedByField.size() > 0) {
            final var count = freqMapGroupedByField.getOrDefault(goldenRecordFieldValue, 0L);
            final var maxEntry = Collections.max(freqMapGroupedByField.entrySet(), Map.Entry.comparingByValue());
            if (isBetterValue(goldenRecordFieldValue, count, maxEntry.getKey(), maxEntry.getValue())) {
               if (LOGGER.isTraceEnabled()) {
                  LOGGER.trace("{}: {} -> {}", fieldName, goldenRecordFieldValue, maxEntry.getKey());
               }
               changed = true;
               final var goldenId = expandedGoldenRecord.goldenRecord().goldenId();
               final var result = libMPI.updateGoldenRecordField(interactionId,
                                                                 goldenId,
                                                                 fieldName,
                                                                 goldenRecordFieldValue,
                                                                 maxEntry.getKey());
               if (!result) {
                  LOGGER.error("libMPI.updateGoldenRecordField({}, {}, {})", goldenId, fieldName, maxEntry.getKey());
               }
/*
               sendAuditEvent(interactionId,
                              goldenId,
                              String.format("%s: '%s' -> '%s'", fieldName, goldenRecordFieldValue, maxEntry.getKey()));
*/
            }
         }
      }
      return changed;
   }

   void helperUpdateInteractionsScore(
         final float threshold,
         final ExpandedGoldenRecord expandedGoldenRecord) {
      if (LOGGER.isTraceEnabled()) {
         expandedGoldenRecord.interactionsWithScore().forEach(interactionWithScore -> {
            LOGGER.trace("{} -> {} : {}",
                         interactionWithScore.interaction().uniqueInteractionData().auxId(),
                         expandedGoldenRecord.goldenRecord().customUniqueGoldenRecordData().auxId(),
                         interactionWithScore.score());
         });
      }
      expandedGoldenRecord.interactionsWithScore().forEach(interactionWithScore -> {
         final var interaction = interactionWithScore.interaction();
         final var score =
               calcNormalizedScore(expandedGoldenRecord.goldenRecord().demographicData(), interaction.demographicData());

         if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} -- {} : {}", interactionWithScore.score(), score, abs(interactionWithScore.score() - score) > 1E-2);
         }
         if (abs(interactionWithScore.score() - score) > 1E-3) {
            final var rc = libMPI.setScore(interaction.interactionId(),
                                           expandedGoldenRecord.goldenRecord().goldenId(),
                                           interactionWithScore.score(),
                                           score);
            if (!rc) {
               LOGGER.error("set score error {} -> {} : {}",
                            interaction.interactionId(),
                            expandedGoldenRecord.goldenRecord().goldenId(),
                            score);
/*
            } else {
               sendAuditEvent(interaction.interactionId(),
                              expandedGoldenRecord.goldenRecord().goldenId(),
                              String.format("score: %.5f -> %.5f", interactionWithScore.score(), score));
               if (LOGGER.isTraceEnabled()) {
                  LOGGER.trace("set score result: {}", rc);
               }
*/
            }
            if (score <= threshold) {
               sendNotification(Notification.NotificationType.UPDATE,
                                interaction.interactionId(),
                                AppUtils.getNames(interaction.demographicData()),
                                new Notification.MatchData(expandedGoldenRecord.goldenRecord().goldenId(), score),
                                List.of());
            }
         }
      });
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

   public record EventUpdateMUReq(
         CustomMU mu,
         ActorRef<EventUpdateMURsp> replyTo) implements Request {
   }

   public record EventUpdateMURsp(boolean rc) implements Response {
   }

   public record EventGetMUReq(ActorRef<EventGetMURsp> replyTo) implements Request {
   }

   public record EventGetMURsp(CustomMU mu) implements Response {
   }

   public record CalculateScoresRequest(
         ApiModels.ApiCalculateScoresRequest calculateScoresRequest,
         ActorRef<CalculateScoresResponse> replyTo) implements Request {

   }

   public record CalculateScoresResponse(
         ApiModels.ApiCalculateScoresResponse calculateScoresResponse) {

   }

   public record SyncLinkInteractionRequest(
         LinkInteractionSyncBody link,
         ActorRef<SyncLinkInteractionResponse> replyTo) implements Request {
   }

   public record SyncLinkInteractionResponse(
         String stan,
         LinkInfo linkInfo,
         List<ExternalLinkCandidate> externalLinkCandidateList) implements Response {
   }

   public record SyncLinkInteractionToGidRequest(
         LinkInteractionToGidSyncBody link,
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

   public record CrFindRequest(
         ApiModels.ApiCrFindRequest crFindData,
         ActorRef<CrFindResponse> replyTo) implements Request {
   }

   public record CrFindResponse(
         Either<MpiGeneralError, List<GoldenRecord>> response) implements Response {
   }

   public record CrUpdateFieldRequest(
         ApiModels.ApiCrUpdateFieldRequest crUpdateField,
         ActorRef<CrUpdateFieldResponse> replyTo) implements Request {
   }

   public record CrUpdateFieldResponse(
         Either<MpiGeneralError, UpdateFieldResponse> response) implements Response {
      public record UpdateFieldResponse(
            String goldenId,
            String name,
            String value) {
      }
   }

}
