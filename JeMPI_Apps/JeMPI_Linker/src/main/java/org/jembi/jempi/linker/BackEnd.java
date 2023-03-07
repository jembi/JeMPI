package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BackEnd extends AbstractBehavior<BackEnd.Event> {

   private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
   private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
   private static LibMPI libMPI = null;
   private MyKafkaProducer<String, Notification> topicNotifications;

   private BackEnd(final ActorContext<Event> context) {
      super(context);
      if (libMPI == null) {
         openMPI();
      }
      topicNotifications = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                 GlobalConstants.TOPIC_NOTIFICATIONS,
                                                 new StringSerializer(), new JsonPojoSerializer<>(),
                                                 AppConfig.KAFKA_CLIENT_ID_NOTIFICATIONS);
   }

   private static void openMPI() {
      final var host = new String[]{AppConfig.DGRAPH_ALPHA1_HOST, AppConfig.DGRAPH_ALPHA2_HOST, AppConfig.DGRAPH_ALPHA3_HOST};
      final var port = new int[]{AppConfig.DGRAPH_ALPHA1_PORT, AppConfig.DGRAPH_ALPHA2_PORT, AppConfig.DGRAPH_ALPHA3_PORT};
      libMPI = new LibMPI(host, port);
      libMPI.startTransaction();
      if (!(libMPI.dropAll().isEmpty() && libMPI.createSchema().isEmpty())) {
         LOGGER.error("Create Schema Error");
      }
      libMPI.closeTransaction();
   }

   public static Behavior<Event> create() {
      return Behaviors.setup(BackEnd::new);
   }
   private BackEnd(final ActorContext<Event> context, final LibMPI lib) {
      super(context);
      this.libMPI = lib;
   }

   public static Behavior<Event> create(final LibMPI lib) {
      return Behaviors.setup(context -> new BackEnd(context, lib));
   }

   private static float calcNormalizedScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData patient) {
      if (Boolean.TRUE.equals(AppConfig.BACK_END_DETERMINISTIC)) {
         final var match = CustomLinkerDeterministic.deterministicMatch(goldenRecord, patient);
         if (match) {
            return 1.0F;
         }
      }
      return CustomLinkerProbabilistic.probabilisticScore(goldenRecord, patient);
   }

   private static boolean isBetterValue(
         final String textLeft,
         final long countLeft,
         final String textRight,
         final long countRight) {
      return (StringUtils.isBlank(textLeft) && countRight >= 1) || (countRight > countLeft && !textRight.equals(textLeft));
   }

   static void updateGoldenRecordField(
         final ExpandedGoldenRecord expandedGoldenRecord,
         final String fieldName,
         final String goldenRecordFieldValue,
         final Function<CustomDemographicData, String> getDocumentField) {

      if (fieldName == null) {
         throw new NullPointerException("fieldName cannot be null");
      }
      if (fieldName.isEmpty()) {
         throw new IllegalArgumentException("fieldName cannot be empty");
      }
      if (expandedGoldenRecord == null) {
         throw new NullPointerException("expandedGoldenRecord cannot be null");
      }
      final var mpiPatientList = expandedGoldenRecord.patientRecordsWithScore();
      final var freqMapGroupedByField =
            mpiPatientList
                  .stream()
                  .map(mpiPatient -> getDocumentField.apply(mpiPatient.patientRecord().demographicData()))
                  .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
      freqMapGroupedByField.remove(StringUtils.EMPTY);
      if (freqMapGroupedByField.size() > 0) {
         final var count = freqMapGroupedByField.getOrDefault(goldenRecordFieldValue, 0L);
         final var maxEntry = Collections.max(freqMapGroupedByField.entrySet(), Map.Entry.comparingByValue());
         if (isBetterValue(goldenRecordFieldValue, count, maxEntry.getKey(), maxEntry.getValue())) {
            final var goldenId = expandedGoldenRecord.goldenRecord().goldenId();
            final var result = libMPI.updateGoldenRecordField(goldenId, fieldName, maxEntry.getKey());
            if (!result) {
               LOGGER.error("libMPI.updateGoldenRecordField({}, {}, {})", goldenId, fieldName, maxEntry.getKey());
            } else if (!mpiPatientList.isEmpty()) {
               updateMatchingPatientRecordScoreForGoldenRecord(expandedGoldenRecord, goldenId);
            }
         }
      }
   }

   static void updateMatchingPatientRecordScoreForGoldenRecord(final ExpandedGoldenRecord expandedGoldenRecord, final String goldenRecordId) {
      final var mpiPatientList = expandedGoldenRecord.patientRecordsWithScore();
      mpiPatientList.forEach(mpiPatient -> {
         final var patient = mpiPatient.patientRecord();
         final var score = calcNormalizedScore(expandedGoldenRecord.goldenRecord().demographicData(), patient.demographicData());
         final var reCompute = libMPI.setScore(patient.patientId(), goldenRecordId, score);
         if (!reCompute) {
            LOGGER.error("Failed to update score for entity with UID {}", patient.patientId());
         } else {
            LOGGER.debug("Successfully updated score for entity with UID {}", patient.patientId());
         }
      });
   }

   @Override
   public Receive<Event> createReceive() {
      return newReceiveBuilder()
            .onMessage(EventLinkPatientAsyncReq.class, this::eventLinkPatientAsyncHandler)
            .onMessage(EventTeaTime.class, this::eventTeaTimeHandler)
            .onMessage(EventWorkTime.class, this::eventWorkTimeHandler)
            .onMessage(EventLinkPatientSyncReq.class, this::eventLinkPatientSyncHandler)
            .onMessage(EventLinkPatientToGidSyncReq.class, this::eventLinkPatientToGidSyncHandler)
            .onMessage(EventUpdateMUReq.class, this::eventUpdateMUReqHandler)
            .onMessage(EventGetMUReq.class, this::eventGetMUReqHandler)
            .onMessage(EventCalculateScoresReq.class, this::eventCalculateScoresHandler)
            .build();
   }

   private Behavior<Event> eventUpdateMUReqHandler(final EventUpdateMUReq req) {
      LOGGER.info("*************** {} **************", req);
      CustomLinkerProbabilistic.updateMU(req.mu);
      req.replyTo.tell(new EventUpdateMURsp(true));
      return Behaviors.same();
   }

   private Behavior<Event> eventWorkTimeHandler(final EventWorkTime request) {
      LOGGER.info("WORK TIME");
      return Behaviors.same();
   }

   private Behavior<Event> eventTeaTimeHandler(final EventTeaTime request) {
      LOGGER.info("TEA TIME");
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventWorkTime.INSTANCE, Duration.ofSeconds(5));
         return Behaviors.same();
      });
   }

   private LinkInfo linkPatientToGid(
         final PatientRecord patientRecord,
         final String gid,
         final float score) {
      final LinkInfo linkInfo;
      try {
         // Check if we have new M&U values
         CustomLinkerProbabilistic.checkUpdatedMU();

         libMPI.startTransaction();
         final var docAuxKey = patientRecord.demographicData().auxId();

         LOGGER.info("{}: no matches found", docAuxKey);

         if (StringUtils.isBlank(gid)) {
            linkInfo = libMPI.createPatientAndLinkToClonedGoldenRecord(patientRecord, 1.0F);
         } else {
            linkInfo = libMPI.createPatientAndLinkToExistingGoldenRecord(
                  patientRecord,
                  new LibMPIClientInterface.GoldenIdScore(gid, score));
            CustomLinkerBackEnd.updateGoldenRecordFields(libMPI, gid);
         }
      } finally {
         libMPI.closeTransaction();
      }
      return linkInfo;
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

   private Either<LinkInfo, List<ExternalLinkCandidate>> linkPatient(
         final String stan,
         final PatientRecord patientRecord,
         final ExternalLinkRange externalLinkRange,
         final float matchThreshold_) {
      LOGGER.debug("{}", stan);
      LinkInfo linkInfo = null;
      final List<ExternalLinkCandidate> externalLinkCandidateList = new ArrayList<>();
      final var matchThreshold = externalLinkRange != null
            ? externalLinkRange.high()
            : matchThreshold_;
      try {
         CustomLinkerProbabilistic.checkUpdatedMU();
         libMPI.startTransaction();
         final var candidateGoldenRecords =
               libMPI.getCandidates(patientRecord.demographicData(), AppConfig.BACK_END_DETERMINISTIC);
         if (candidateGoldenRecords.isEmpty()) {
            linkInfo = libMPI.createPatientAndLinkToClonedGoldenRecord(patientRecord, 1.0F);
         } else {
            final var allCandidateScores =
                  candidateGoldenRecords
                        .parallelStream()
                        .unordered()
                        .map(candidate -> new WorkCandidate(candidate, calcNormalizedScore(candidate.demographicData(),
                                                                                           patientRecord.demographicData())))
                        .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                        .collect(Collectors.toCollection(ArrayList::new));

            // Get a list of candidates withing the supplied for external link range
            final var candidatesInExternalLinkRange =
                  externalLinkRange == null
                        ? new ArrayList<WorkCandidate>()
                        : allCandidateScores
                              .stream()
                              .filter(v -> v.score() >= externalLinkRange.low() && v.score() <= externalLinkRange.high())
                              .collect(Collectors.toCollection(ArrayList::new));

            // Get a list of candidates above the supplied threshold
            final var notificationCandidates = new ArrayList<Notification.MatchData>();
            final var candidatesAboveMatchThreshold = allCandidateScores
                  .stream()
                  .peek(v -> {
                     if (v.score() >= matchThreshold - 0.1 && v.score() <= matchThreshold + 0.1) {
                        notificationCandidates.add(new Notification.MatchData(v.goldenRecord().goldenId(), v.score()));
                     }
                  })
                  .filter(v -> v.score() >= matchThreshold)
                  .collect(Collectors.toCollection(ArrayList::new));

            if (candidatesAboveMatchThreshold.isEmpty()) {
               if (candidatesInExternalLinkRange.isEmpty()) {
                  linkInfo = libMPI.createPatientAndLinkToClonedGoldenRecord(patientRecord, 1.0F);
                  if (!notificationCandidates.isEmpty()) {
                     sendNotification(
                           Notification.NotificationType.THRESHOLD,
                           linkInfo.patientUID(),
                           AppUtils.getNames(patientRecord.demographicData()),
                           new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                           notificationCandidates
                                     );
                  }
               } else {
                  candidatesInExternalLinkRange.forEach(
                        candidate -> externalLinkCandidateList.add(new ExternalLinkCandidate(candidate.goldenRecord,
                                                                                             candidate.score)));
               }
            } else {
               final var linkToGoldenId = new LibMPIClientInterface.GoldenIdScore(
                     candidatesAboveMatchThreshold.get(0).goldenRecord.goldenId(),
                     candidatesAboveMatchThreshold.get(0).score);
               linkInfo = libMPI.createPatientAndLinkToExistingGoldenRecord(patientRecord, linkToGoldenId);
               CustomLinkerBackEnd.updateGoldenRecordFields(libMPI, linkToGoldenId.goldenId());

               final var marginalCandidates = new ArrayList<Notification.MatchData>();
               if (candidatesInExternalLinkRange.isEmpty() && candidatesAboveMatchThreshold.size() > 1) {
                  var firstCandidate = candidatesAboveMatchThreshold.get(0);
                  for (var i = 1; i < candidatesAboveMatchThreshold.size(); i++) {
                     final var candidate = candidatesAboveMatchThreshold.get(i);
                     if (firstCandidate.score - candidate.score <= 0.1) {
                        marginalCandidates.add(new Notification.MatchData(candidate.goldenRecord.goldenId(), candidate.score));
                     } else {
                        break;
                     }
                  }
                  if (!marginalCandidates.isEmpty()) {
                     sendNotification(
                           Notification.NotificationType.MARGIN,
                           linkInfo.patientUID(),
                           AppUtils.getNames(patientRecord.demographicData()),
                           new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                           marginalCandidates);
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

   private CalculateScoresResponse calculateScores(final CalculateScoresRequest request) {
      final var patientRecord = libMPI.findPatientRecord(request.patientId());
      final var goldenRecords = libMPI.findGoldenRecords(request.goldenIds());
      LOGGER.debug("{}", patientRecord);
      LOGGER.debug("{}", goldenRecords);
      final var scores = goldenRecords
            .parallelStream()
            .unordered()
            .map(goldenRecord -> new CalculateScoresResponse.Score(
                  goldenRecord.goldenId(),
                  calcNormalizedScore(goldenRecord.demographicData(), patientRecord.demographicData())))
            .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
            .collect(Collectors.toCollection(ArrayList::new));
      return new CalculateScoresResponse(request.patientId(), scores);
   }

   private Behavior<Event> eventCalculateScoresHandler(final EventCalculateScoresReq req) {
      req.replyTo.tell(new EventCalculateScoresRsp(calculateScores(req.calculateScoresRequest)));
      return Behaviors.same();
   }

   private Behavior<Event> eventGetMUReqHandler(final EventGetMUReq req) {
      req.replyTo.tell(new EventGetMURsp(CustomLinkerProbabilistic.getMU()));
      return Behaviors.same();
   }

   private Behavior<Event> eventLinkPatientAsyncHandler(final EventLinkPatientAsyncReq req) {
      if (req.batchPatientRecord.batchType() != BatchPatientRecord.BatchType.BATCH_PATIENT) {
         return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventTeaTime.INSTANCE, Duration.ofSeconds(5));
            req.replyTo.tell(new EventLinkPatientAsyncRsp(null));
            return Behaviors.same();
         });
      }
      final var listLinkInfo = linkPatient(
            req.batchPatientRecord.stan(),
            req.batchPatientRecord.patientRecord(),
            null,
            AppConfig.BACK_END_MATCH_THRESHOLD);
      // TODO   send link info to kafka notification topic
      req.replyTo.tell(new EventLinkPatientAsyncRsp(listLinkInfo.getLeft()));
      return Behaviors.withTimers(timers -> {
         timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventTeaTime.INSTANCE, Duration.ofSeconds(30));
         return Behaviors.same();
      });
   }

   private Behavior<Event> eventLinkPatientSyncHandler(final EventLinkPatientSyncReq request) {
      final var listLinkInfo = linkPatient(
            request.link.stan(),
            request.link.patientRecord(),
            request.link.externalLinkRange(),
            request.link.matchThreshold());
      request.replyTo.tell(new EventLinkPatientSyncRsp(request.link.stan(),
                                                       listLinkInfo.isLeft()
                                                             ? listLinkInfo.getLeft()
                                                             : null,
                                                       listLinkInfo.isRight()
                                                             ? listLinkInfo.get()
                                                             : null));
      return Behaviors.same();
   }

   private Behavior<Event> eventLinkPatientToGidSyncHandler(final EventLinkPatientToGidSyncReq request) {
      final var linkInfo = linkPatientToGid(
            request.link.patientRecord(),
            request.link.gid(),
            3.0F);
      request.replyTo.tell(new EventLinkPatientToGidSyncRsp(request.link.stan(), linkInfo));
      return Behaviors.same();
   }


   private enum EventTeaTime implements Event {
      INSTANCE
   }

   private enum EventWorkTime implements Event {
      INSTANCE
   }

   interface Event {
   }

   interface EventResponse {
   }

   private record WorkCandidate(
         GoldenRecord goldenRecord,
         float score) {
   }

   public record EventLinkPatientAsyncReq(
         String key,
         BatchPatientRecord batchPatientRecord,
         ActorRef<EventLinkPatientAsyncRsp> replyTo) implements Event {
   }

   public record EventLinkPatientAsyncRsp(LinkInfo linkInfo) implements EventResponse {
   }

   public record EventUpdateMUReq(
         CustomMU mu,
         ActorRef<EventUpdateMURsp> replyTo) implements Event {
   }

   public record EventUpdateMURsp(boolean rc) implements EventResponse {
   }

   public record EventGetMUReq(ActorRef<EventGetMURsp> replyTo) implements Event {
   }

   public record EventGetMURsp(CustomMU mu) implements EventResponse {
   }

   public record EventCalculateScoresReq(
         CalculateScoresRequest calculateScoresRequest,
         ActorRef<EventCalculateScoresRsp> replyTo) implements Event {

   }

   public record EventCalculateScoresRsp(
         CalculateScoresResponse calculateScoresResponse) {

   }

   public record EventLinkPatientSyncReq(
         LinkPatientSyncBody link,
         ActorRef<EventLinkPatientSyncRsp> replyTo) implements Event {
   }

   public record EventLinkPatientSyncRsp(
         String stan,
         LinkInfo linkInfo,
         List<ExternalLinkCandidate> externalLinkCandidateList) implements EventResponse {
   }

   public record EventLinkPatientToGidSyncReq(
         LinkPatientToGidSyncBody link,
         ActorRef<EventLinkPatientToGidSyncRsp> replyTo) implements Event {
   }


   public record EventLinkPatientToGidSyncRsp(
         String stan,
         LinkInfo linkInfo) implements EventResponse {
   }


}
