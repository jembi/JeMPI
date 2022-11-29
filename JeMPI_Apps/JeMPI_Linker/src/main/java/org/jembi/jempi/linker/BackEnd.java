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
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BackEnd extends AbstractBehavior<BackEnd.Event> {

    private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);
    private static final String SINGLE_TIMER_TIMEOUT_KEY = "SingleTimerTimeOutKey";
    private static LibMPI libMPI = null;

    private final CustomLinkerMU customLinkerMU = new CustomLinkerMU();

    private MyKafkaProducer<String, Notification> topicNotifications;

    private BackEnd(ActorContext<Event> context) {
        super(context);
        if (libMPI == null) {
            openMPI();
        }
        topicNotifications = new MyKafkaProducer<>(GlobalConstants.TOPIC_NOTIFICATIONS,
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

    private static float calcNormalizedScore(final CustomGoldenRecord goldenRecord, final CustomEntity document) {
        if (Boolean.TRUE.equals(AppConfig.BACK_END_DETERMINISTIC)) {
            final var match = CustomLinkerDeterministic.deterministicMatch(goldenRecord, document);
            if (match) {
                return 1.0F;
            }
        }
        return CustomLinkerProbabilistic.probabilisticScore(goldenRecord, document);
    }

    private static boolean isBetterValue(final String textLeft, final long countLeft, final String textRight,
                                         final long countRight) {
        return (StringUtils.isBlank(textLeft) && countRight >= 1) || (countRight > countLeft && !textRight.equals(textLeft));
    }

    static void updateGoldenRecordField(final MpiExpandedGoldenRecord expandedGoldenRecord,
                                        final String predicate,
                                        final String goldenRecordFieldValue,
                                        final Function<CustomEntity, String> getDocumentField) {
        final var mpiEntityList = expandedGoldenRecord.mpiEntityList();
        final var freqMapGroupedByField =
                mpiEntityList
                        .stream()
                        .map(mpiEntity -> getDocumentField.apply(mpiEntity.entity()))
                        .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        freqMapGroupedByField.remove(StringUtils.EMPTY);
        if (freqMapGroupedByField.size() > 0) {
            final var count = freqMapGroupedByField.getOrDefault(goldenRecordFieldValue, 0L);
            final var maxEntry = Collections.max(freqMapGroupedByField.entrySet(), Map.Entry.comparingByValue());
            if (isBetterValue(goldenRecordFieldValue, count, maxEntry.getKey(), maxEntry.getValue())) {
                final var uid = expandedGoldenRecord.customGoldenRecord().uid();
                final var result = libMPI.updateGoldenRecordPredicate(uid, predicate, maxEntry.getKey());
                if (!result) {
                    LOGGER.error("libMPI.updateGoldenRecordPredicate({}, {}, {})", uid, predicate, maxEntry.getKey());
                }
            }
        }
    }

    @Override
    public Receive<Event> createReceive() {
        return newReceiveBuilder()
                .onMessage(EventLinkEntityAsyncReq.class, this::eventLinkEntityAsyncHandler)
                .onMessage(EventTeaTime.class, this::eventTeaTimeHandler)
                .onMessage(EventWorkTime.class, this::eventWorkTimeHandler)
                .onMessage(EventLinkEntitySyncReq.class, this::eventLinkEntitySyncHandler)
                .onMessage(EventLinkEntityToGidSyncReq.class, this::eventLinkEntityToGidSyncHandler)
                .onMessage(EventUpdateMUReq.class, this::eventUpdateMUReqHandler)
                .onMessage(EventGetMUReq.class, this::eventGetMUReqHandler)
                .build();

    }

    private Behavior<Event> eventUpdateMUReqHandler(EventUpdateMUReq req) {
        LOGGER.info("*************** {} **************", req);
        CustomLinkerProbabilistic.updateMU(req.mu);
        req.replyTo.tell(new EventUpdateMURsp(true));
        return Behaviors.same();
    }

    private Behavior<Event> eventWorkTimeHandler(EventWorkTime request) {
        LOGGER.info("WORK TIME");
        return Behaviors.same();
    }

    private Behavior<Event> eventTeaTimeHandler(EventTeaTime request) {
        LOGGER.info("TEA TIME");
        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventWorkTime.INSTANCE, Duration.ofSeconds(5));
            return Behaviors.same();
        });
    }

    private LibMPIClientInterface.LinkInfo linkEntityToGid(final CustomEntity entity,
                                                           final String gid,
                                                           final float score) {
        final LibMPIClientInterface.LinkInfo linkInfo;
        try {
            // Check if we have new M&U values
            CustomLinkerProbabilistic.checkUpdatedMU();

            libMPI.startTransaction();
            final var docAuxKey = entity.auxId();

            LOGGER.info("{}: no matches found", docAuxKey);

            if (StringUtils.isBlank(gid)) {
                linkInfo = libMPI.createEntityAndLinkToClonedGoldenRecord(entity, 1.0F);
            } else {
                linkInfo = libMPI.createEntityAndLinkToExistingGoldenRecord(entity,
                        new LibMPIClientInterface.GoldenIdScore(gid, score));
                CustomLinkerBackEnd.updateGoldenRecordFields(libMPI, gid);
            }
        } finally {
            libMPI.closeTransaction();
        }
        return linkInfo;
    }

    private void sendNotification(final Notification.NotificationType type,
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

    private Either<LibMPIClientInterface.LinkInfo, List<ExternalLinkCandidate>>
    linkEntity(final String stan, final CustomEntity customEntity, final ExternalLinkRange externalLinkRange,
               final float matchThreshold_) {
        LOGGER.debug("{}", stan);
        LibMPIClientInterface.LinkInfo linkInfo = null;
        final List<ExternalLinkCandidate> externalLinkCandidateList = new ArrayList<>();
        final var matchThreshold = externalLinkRange != null ? externalLinkRange.high() : matchThreshold_;
        try {
            CustomLinkerProbabilistic.checkUpdatedMU();
            libMPI.startTransaction();
            final var candidateGoldenRecords = libMPI.getCandidates(customEntity, AppConfig.BACK_END_DETERMINISTIC);
            if (candidateGoldenRecords.isEmpty()) {
                linkInfo = libMPI.createEntityAndLinkToClonedGoldenRecord(customEntity, 1.0F);
            } else {
//                final var notification = new Notification(
//                        System.currentTimeMillis(),
//                        Notification.NotificationType.MARGIN,
//                        "0x01",
//                        new ArrayList<>(Arrays.asList("Geek", "for", "Geeks")),
//                        new Notification.MatchData("0x02", 0.9F),
//                        new ArrayList<>(List.of(new Notification.MatchData("0x03", 0.89F)))
//                );
//                try {
//                    topicNotifications.produceSync("dummy", notification);
//                } catch (ExecutionException | InterruptedException e) {
//                    LOGGER.error(e.getLocalizedMessage(), e);
//                }
                final var allCandidateScores =
                        candidateGoldenRecords
                                .parallelStream()
                                .unordered()
                                .map(candidate -> new WorkCandidate(candidate, calcNormalizedScore(candidate, customEntity)))
                                .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                .collect(Collectors.toCollection(ArrayList::new));
/*
            if (!allCandidateScores.isEmpty()) {
               for (int i = 0; i < allCandidateScores.size(); i++) {
                  final var candidate = allCandidateScores.get(i);
                  if (i == 0 && candidate.score() > matchThreshold) {
                     customLinkerMU.updateMatchSums(customEntity, candidate.goldenRecord());
                  } else {
                     customLinkerMU.updateMissmatchSums(customEntity, candidate.goldenRecord());
                  }
               }
            }
*/
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
                                notificationCandidates.add(new Notification.MatchData(v.goldenRecord().uid(), v.score()));
                            }
                        })
                        .filter(v -> v.score() >= matchThreshold)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (candidatesAboveMatchThreshold.isEmpty()) {
                    if (candidatesInExternalLinkRange.isEmpty()) {
                        linkInfo = libMPI.createEntityAndLinkToClonedGoldenRecord(customEntity, 1.0F);
                        if (!notificationCandidates.isEmpty()) {
                            sendNotification(
                                    Notification.NotificationType.THRESHOLD,
                                    linkInfo.entityId(),
                                    customEntity.getNames(customEntity),
                                    new Notification.MatchData(linkInfo.goldenId(), 1.0F),
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
                            candidatesAboveMatchThreshold.get(0).goldenRecord.uid(),
                            candidatesAboveMatchThreshold.get(0).score);
                    linkInfo = libMPI.createEntityAndLinkToExistingGoldenRecord(customEntity, linkToGoldenId);
                    CustomLinkerBackEnd.updateGoldenRecordFields(libMPI, linkToGoldenId.goldenId());
                }
            }
        } finally {
            libMPI.closeTransaction();
        }
        return linkInfo == null
                ? Either.right(externalLinkCandidateList)
                : Either.left(linkInfo);
    }

    private Behavior<Event> eventGetMUReqHandler(EventGetMUReq req) {
        req.replyTo.tell(new EventGetMURsp(CustomLinkerProbabilistic.getMU()));
        return Behaviors.same();
    }

    private Behavior<Event> eventLinkEntityAsyncHandler(EventLinkEntityAsyncReq req) {
        if (req.batchEntity.entityType() != BatchEntity.EntityType.BATCH_RECORD) {
            return Behaviors.withTimers(timers -> {
                timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventTeaTime.INSTANCE, Duration.ofSeconds(5));
                req.replyTo.tell(new EventLinkEntityAsyncRsp(null));
                return Behaviors.same();
            });
        }
        final var listLinkInfo = linkEntity(
                req.batchEntity.stan(),
                req.batchEntity.entity(),
                null,
                AppConfig.BACK_END_MATCH_THRESHOLD);
        // TODO   send link info to kafka notification topic
        req.replyTo.tell(new EventLinkEntityAsyncRsp(listLinkInfo.getLeft()));
        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(SINGLE_TIMER_TIMEOUT_KEY, EventTeaTime.INSTANCE, Duration.ofSeconds(30));
            return Behaviors.same();
        });
    }

    private Behavior<Event> eventLinkEntitySyncHandler(EventLinkEntitySyncReq request) {
        final var listLinkInfo = linkEntity(
                request.link.stan(),
                request.link.entity(),
                request.link.externalLinkRange(),
                request.link.matchThreshold());
        request.replyTo.tell(new EventLinkEntitySyncRsp(request.link.stan(),
                listLinkInfo.isLeft() ? listLinkInfo.getLeft() : null,
                listLinkInfo.isRight() ? listLinkInfo.get() : null));
        return Behaviors.same();
    }

    private Behavior<Event> eventLinkEntityToGidSyncHandler(EventLinkEntityToGidSyncReq request) {
        final var linkInfo = linkEntityToGid(
                request.link.entity(),
                request.link.gid(),
                3.0F);
        request.replyTo.tell(new EventLinkEntityToGidSyncRsp(request.link.stan(), linkInfo));
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
            CustomGoldenRecord goldenRecord,
            float score) {
    }

    public record EventLinkEntityAsyncReq(String key, BatchEntity batchEntity,
                                          ActorRef<EventLinkEntityAsyncRsp> replyTo) implements Event {
    }

    public record EventLinkEntityAsyncRsp(LibMPIClientInterface.LinkInfo linkInfo) implements EventResponse {
    }

    public record EventUpdateMUReq(CustomMU mu, ActorRef<EventUpdateMURsp> replyTo) implements Event {
    }

    public record EventUpdateMURsp(boolean rc) implements EventResponse {
    }

    public record EventGetMUReq(ActorRef<EventGetMURsp> replyTo) implements Event {
    }

    public record EventGetMURsp(CustomMU mu) implements EventResponse {
    }

    public record EventLinkEntitySyncReq(LinkEntitySyncBody link,
                                         ActorRef<EventLinkEntitySyncRsp> replyTo) implements Event {
    }

    public record EventLinkEntitySyncRsp(String stan,
                                         LibMPIClientInterface.LinkInfo linkInfo,
                                         List<ExternalLinkCandidate> externalLinkCandidateList) implements EventResponse {
    }

    public record EventLinkEntityToGidSyncReq(LinkEntityToGidSyncBody link,
                                              ActorRef<EventLinkEntityToGidSyncRsp> replyTo) implements Event {
    }

    public record ExternalLinkCandidate(CustomGoldenRecord goldenRecord, float score) {
    }

    public record EventLinkEntityToGidSyncRsp(String stan,
                                              LibMPIClientInterface.LinkInfo linkInfo) implements EventResponse {
    }


}
