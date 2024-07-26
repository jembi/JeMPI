package org.jembi.jempi.linker.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.shared.config.linker.Programs;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static org.jembi.jempi.shared.config.Config.FIELDS_CONFIG;
import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;
import static org.jembi.jempi.shared.models.FieldTallies.CUSTOM_FIELD_TALLIES_SUM_IDENTITY;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

/**
 * The type Linker dwh.
 */
public final class LinkerDWH {

   private static final Logger LOGGER = LogManager.getLogger(LinkerDWH.class);

   private static MyKafkaProducer<String, LinkStatsMeta> linkStatsMetaProducer = null;
   private static MyKafkaProducer<String, MatchNotification> matchNoficationProducer = null;

   private LinkerDWH() {
   }

   private static boolean isBetterValue(
         final String textLeft,
         final long countLeft,
         final String textRight,
         final long countRight) {
      return (StringUtils.isBlank(textLeft) && countRight >= 1) || (countRight > countLeft && !textRight.equals(textLeft));
   }

   private static boolean helperUpdateGoldenRecordField(
         final LibMPI libMPI,
         final String interactionId,
         final ExpandedGoldenRecord expandedGoldenRecord,
         final String fieldName,
         final String goldenRecordFieldValue,
         final Stream<String> interactionFieldValues) {

      boolean changed = false;

      if (expandedGoldenRecord == null) {
         LOGGER.error("expandedGoldenRecord cannot be null");
      } else {
         final var freqMapGroupedByField = interactionFieldValues.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

         freqMapGroupedByField.remove(StringUtils.EMPTY);
         if (!freqMapGroupedByField.isEmpty()) {
            final var count = freqMapGroupedByField.getOrDefault(goldenRecordFieldValue, 0L);
            final var maxEntry = Collections.max(freqMapGroupedByField.entrySet(), Map.Entry.comparingByValue());
            if (isBetterValue(goldenRecordFieldValue, count, maxEntry.getKey(), maxEntry.getValue())) {
               if (LOGGER.isTraceEnabled()) {
                  LOGGER.trace("{}: {} -> {}", fieldName, goldenRecordFieldValue, maxEntry.getKey());
               }
               final var i = FIELDS_CONFIG.findIndexOfDemographicField(fieldName);
               if (i < 0) {
                  LOGGER.error("{}", fieldName);
               } else {
                  changed = true;
                  final var goldenId = expandedGoldenRecord.goldenRecord().goldenId();
                  final var result = libMPI.updateGoldenRecordField(interactionId,
                                                                    goldenId,
                                                                    fieldName,
                                                                    goldenRecordFieldValue,
                                                                    maxEntry.getKey(),
                                                                    fieldName);
                  if (!result) {
                     LOGGER.error("libMPI.updateGoldenRecordField({}, {}, {})", goldenId, fieldName, maxEntry.getKey());
                  }
               }
            }
         }
      }
      return changed;
   }

   private static String patientName(final Interaction interaction) {
      var patientRecord = interaction.demographicData();
      String givenName = patientRecord.fields.stream()
         .filter(field -> "given_name".equals(field.ccTag()))
         .map(DemographicData.DemographicField::value)
         .findFirst()
         .orElse("");
      String familyName = patientRecord.fields.stream()
         .filter(field -> "family_name".equals(field.ccTag()))
         .map(DemographicData.DemographicField::value)
         .findFirst()
         .orElse("");
         return (givenName + " " + familyName).trim();
   }

   /**
    * Helper update interactions score.
    *
    * @param libMPI               the lib mpi
    * @param threshold            the threshold
    * @param expandedGoldenRecord the expanded golden record
    */
   static void helperUpdateInteractionsScore(
         final LibMPI libMPI,
         final float threshold,
         final ExpandedGoldenRecord expandedGoldenRecord) {
      expandedGoldenRecord.interactionsWithScore().forEach(interactionWithScore -> {
         final var interaction = interactionWithScore.interaction();
         final var score = LinkerUtils.calcNormalizedLinkScore(expandedGoldenRecord.goldenRecord().demographicData(),
                                                               interaction.demographicData());

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
            }
            if (score <= threshold) {
               sendNotification(Notification.NotificationType.UPDATE,
                                interaction.interactionId(),
                                patientName(interaction),
                                new Notification.MatchData(expandedGoldenRecord.goldenRecord().goldenId(), score),
                                List.of());
            }
         }
      });
   }

   private static void updateGoldenRecordFields(
         final LibMPI libMPI,
         final float threshold,
         final String interactionId,
         final String goldenId) {
      final var expandedGoldenRecord = libMPI.findExpandedGoldenRecords(List.of(goldenId)).getFirst();
      final var goldenRecord = expandedGoldenRecord.goldenRecord();
      final var demographicData = goldenRecord.demographicData();
      var k = 0;

      for (int f = 0; f < demographicData.fields.size(); f++) {
         final int finalF = f;
         k += LinkerDWH.helperUpdateGoldenRecordField(libMPI, interactionId, expandedGoldenRecord,
                                                      demographicData.fields.get(finalF).ccTag(),
                                                      demographicData.fields.get(finalF).value(),
                                                      expandedGoldenRecord.interactionsWithScore()
                                                                          .stream()
                                                                          .map(rec -> rec
                                                                                .interaction()
                                                                                .demographicData().fields.get(finalF)
                                                                                                         .value()))
               ? 1
               : 0;
      }
      if (k > 0) {
         LinkerDWH.helperUpdateInteractionsScore(libMPI, threshold, expandedGoldenRecord);
      }

   }

   private static Either<List<ExternalLinkCandidate>, LinkInfo> doMatch(
         final LibMPI libMPI,
         final Interaction interaction) {
      if (!LINKER_CONFIG.deterministicMatchPrograms.isEmpty() || MUPacket.MATCH_MU_FIELD_COUNT > 0) {
         final var candidates = libMPI.findMatchCandidates(interaction.demographicData());
         LOGGER.debug("Match Candidates {} ", candidates.size());
         if (candidates.isEmpty()) {
            try {
               final var i = OBJECT_MAPPER.writeValueAsString(interaction.demographicData());
               final var f = """
                             MATCH NOTIFICATION NO CANDIDATE
                             {}""";
               LOGGER.info(f, i);
            } catch (JsonProcessingException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            }
         } else {
            final var workCandidate = candidates.parallelStream()
                                                .unordered()
                                                .map(candidate -> new WorkCandidate(candidate,
                                                                                    LinkerUtils.calcNormalizedMatchScore(
                                                                                          candidate.demographicData(),
                                                                                          interaction.demographicData()),
                                                                                    LinkerUtils.determineMatchRule(
                                                                                          candidate.demographicData(),
                                                                                          interaction.demographicData())
                                                ))
                                                .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                                .collect(Collectors.toCollection(ArrayList::new))
                                                .getFirst();
            try {
               final var i = OBJECT_MAPPER.writeValueAsString(interaction.demographicData());
               final var g = OBJECT_MAPPER.writeValueAsString(workCandidate.goldenRecord().demographicData());
               final var f = """
                             MATCH NOTIFICATION
                             {}
                             {}""";
               LOGGER.info(f, i, g);

               if (matchNoficationProducer == null) {
                  matchNoficationProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                                  GlobalConstants.TOPIC_INTERACTION_MATCH,
                                                                  stringSerializer(),
                                                                  matchNotificationSerializer(),
                                                                  "LinkerDWH-INTERACTION-MATCH-NOTIFICATIONS");
               }

               matchNoficationProducer.produceSync(UUID.randomUUID().toString(),
                                                   new MatchNotification(interaction,
                                                                         new GoldenRecordWithScore(workCandidate.goldenRecord,
                                                                                                   workCandidate.score)));
            } catch (JsonProcessingException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            } catch (ExecutionException e) {
               throw new RuntimeException(e);
            } catch (InterruptedException e) {
               LOGGER.error("matchNotificationProducer failed with error: {}", e.getLocalizedMessage());
            }
         }
         return Either.left(List.of());
      } else {
         //create golden record when only link deterministic rules exist and no match rules configured
         var linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
         return linkInfo == null
               ? Either.left(List.of())
               : Either.right(linkInfo);
      }
   }

   /**
    * Link interaction either.
    *
    * @param libMPI            the lib mpi
    * @param interaction       the interaction
    * @param externalLinkRange the external link range
    * @param matchThreshold_   the match threshold
    * @param envelopStan       the envelop stan
    * @return the either
    */
   static Either<List<ExternalLinkCandidate>, LinkInfo> linkInteraction(
         final LibMPI libMPI,
         final Interaction interaction,
         final ExternalLinkRange externalLinkRange,
         final float matchThreshold_,
         final String envelopStan) {

      LinkStatsMeta.ConfusionMatrix confusionMatrix;
      FieldTallies fieldTallies = CUSTOM_FIELD_TALLIES_SUM_IDENTITY;

      if (linkStatsMetaProducer == null) {
         linkStatsMetaProducer = new MyKafkaProducer<>(AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                                                       GlobalConstants.TOPIC_INTERACTION_PROCESSOR_CONTROLLER,
                                                       stringSerializer(),
                                                       linkStatsMetaSerializer(),
                                                       "LinkerDWH-MU-TALLIES");
      }

      if (!Programs.canApplyLinking(LINKER_CONFIG.probabilisticLinkFields,
                                    LINKER_CONFIG.deterministicLinkPrograms,
                                    interaction.demographicData())) {
         return doMatch(libMPI, interaction);
      } else {
         LinkInfo linkInfo = null;
         final List<ExternalLinkCandidate> externalLinkCandidateList = new ArrayList<>();
         final var matchThreshold = externalLinkRange != null
               ? externalLinkRange.high()
               : matchThreshold_;
         LinkerProbabilistic.checkUpdatedLinkMU();
         final var candidateGoldenRecords = libMPI.findLinkCandidates(interaction.demographicData());
         LOGGER.debug("{} : {}", envelopStan, candidateGoldenRecords.size());
         if (candidateGoldenRecords.isEmpty()) {
            linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
            confusionMatrix = new LinkStatsMeta.ConfusionMatrix(0.0, 0.0, 1.0, 0.0);
         } else {
            final var allCandidateScores = candidateGoldenRecords
                  .parallelStream()
                  .unordered()
                  .map(candidate -> new WorkCandidate(candidate,
                                                      LinkerUtils.calcNormalizedLinkScore(
                                                            candidate.demographicData(),
                                                            interaction.demographicData()),
                                                      LinkerUtils.determineLinkRule(
                                                            candidate.demographicData(),
                                                            interaction.demographicData())
                  ))
                  .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                  .collect(Collectors.toCollection(ArrayList::new));

            // DO SOME TALLYING
            fieldTallies = IntStream
                  .range(0, allCandidateScores.size())
                  .parallel()
                  .mapToObj(i -> {
                     final var workCandidate = allCandidateScores.get(i);
                     return FieldTallies.map(i == 0 && workCandidate.score >= matchThreshold,
                                             interaction.demographicData(),
                                             workCandidate.goldenRecord.demographicData());
                  })
                  .reduce(CUSTOM_FIELD_TALLIES_SUM_IDENTITY, FieldTallies::sum);
            final var score = allCandidateScores.getFirst().score;
            if (score >= matchThreshold + 0.1) {
               confusionMatrix = new LinkStatsMeta.ConfusionMatrix(1.0, 0.0, 0.0, 0.0);
            } else if (score >= matchThreshold) {
               confusionMatrix = new LinkStatsMeta.ConfusionMatrix(0.80, 0.20, 0.0, 0.0);
            } else if (score >= matchThreshold - 0.1) {
               confusionMatrix = new LinkStatsMeta.ConfusionMatrix(0.0, 0.0, 0.20, 0.80);
            } else {
               confusionMatrix = new LinkStatsMeta.ConfusionMatrix(0.0, 0.0, 1.0, 0.0);
            }

            // Get a list of candidates within the supplied for external link range
            final var candidatesInExternalLinkRange = externalLinkRange == null
                  ? new ArrayList<WorkCandidate>()
                  : allCandidateScores.stream()
                                      .filter(v -> v.score() >= externalLinkRange.low() && v.score() <= externalLinkRange.high())
                                      .collect(Collectors.toCollection(ArrayList::new));

            // Get a list of candidates above the supplied threshold
            final var belowThresholdNotifications = new ArrayList<Notification.MatchData>();
            final var aboveThresholdNotifications = new ArrayList<Notification.MatchData>();
            final var candidatesAboveMatchThreshold = allCandidateScores.stream().peek(v -> {
               if (v.score() > matchThreshold - 0.1 && v.score() < matchThreshold) {
                  belowThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(), v.score()));
               } else if (v.score() >= matchThreshold && v.score() < matchThreshold + 0.1) {
                  aboveThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(), v.score()));
               }
            }).filter(v -> v.score() >= matchThreshold).collect(Collectors.toCollection(ArrayList::new));
            if (candidatesAboveMatchThreshold.isEmpty()) {
               if (candidatesInExternalLinkRange.isEmpty()) {
                  linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
                  if (!belowThresholdNotifications.isEmpty()) {
                     sendNotification(Notification.NotificationType.BELOW_THRESHOLD,
                                      linkInfo.interactionUID(),
                                      patientName(interaction),
                                      new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                      belowThresholdNotifications);
                  }
               } else {
                  candidatesInExternalLinkRange.forEach(candidate -> externalLinkCandidateList.add(new ExternalLinkCandidate(
                        candidate.goldenRecord,
                        candidate.score)));
               }
            } else {
               final var firstCandidate = candidatesAboveMatchThreshold.getFirst();
               final var linkToGoldenId =
                     new LibMPIClientInterface.GoldenIdScore(firstCandidate.goldenRecord.goldenId(), firstCandidate.score);
               final var validated1 =
                     LinkerDeterministic.validateDeterministicMatch(firstCandidate.goldenRecord.demographicData(),
                                                                    interaction.demographicData());
               final var validated2 =
                     LinkerProbabilistic.validateProbabilisticScore(firstCandidate.goldenRecord.demographicData(),
                                                                    interaction.demographicData());
               linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction,
                                                                                linkToGoldenId,
                                                                                validated1,
                                                                                validated2,
                                                                                firstCandidate.linkingRule());
               if (linkToGoldenId.score() <= matchThreshold + 0.1) {
                  sendNotification(Notification.NotificationType.ABOVE_THRESHOLD,
                                   linkInfo.interactionUID(),
                                   patientName(interaction),
                                   new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                   aboveThresholdNotifications.stream()
                                                              .filter(m -> !Objects.equals(m.gID(),
                                                                                           firstCandidate.goldenRecord.goldenId()))
                                                              .collect(Collectors.toCollection(ArrayList::new)));
               }
               if (Boolean.TRUE.equals(firstCandidate.goldenRecord.auxGoldenRecordData().auxAutoUpdateEnabled())) {
                  updateGoldenRecordFields(libMPI,
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
                                      patientName(interaction),
                                      new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                      marginCandidates);
                  }
               }
            }
         }
         linkStatsMetaProducer.produceAsync("123",
                                            new LinkStatsMeta(confusionMatrix, fieldTallies),
                                            ((metadata, exception) -> {
                                               if (exception != null) {
                                                  LOGGER.error(exception.toString());
                                               }
                                            }));

         return linkInfo == null
               ? Either.left(externalLinkCandidateList)
               : Either.right(linkInfo);
      }
   }

   private static void sendNotification(
         final Notification.NotificationType type,
         final String dID,
         final String names,
         final Notification.MatchData linkedTo,
         final List<Notification.MatchData> candidates) {
      final var notification = new Notification(System.currentTimeMillis(), type, dID, names, linkedTo, candidates);
      try {
         BackEnd.topicNotifications.produceSync("dummy", notification);
      } catch (ExecutionException | InterruptedException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   private static Serializer<String> stringSerializer() {
      return new StringSerializer();
   }

   private static Serializer<LinkStatsMeta> linkStatsMetaSerializer() {
      return new JsonPojoSerializer<>();
   }

   private static Serializer<MatchNotification> matchNotificationSerializer() {
      return new JsonPojoSerializer<>();
   }

   /**
    * The type Work candidate.
    */
   public record WorkCandidate(
         GoldenRecord goldenRecord,
         float score,
         LinkingRule linkingRule) {
   }

}
