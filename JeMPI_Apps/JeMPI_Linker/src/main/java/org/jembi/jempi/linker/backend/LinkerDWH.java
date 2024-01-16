package org.jembi.jempi.linker.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.shared.libs.interactionProcessor.InteractionProcessorConnector;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class LinkerDWH {

   private static final Logger LOGGER = LogManager.getLogger(LinkerDWH.class);

   private LinkerDWH() {
   }

   private static boolean isBetterValue(
         final String textLeft,
         final long countLeft,
         final String textRight,
         final long countRight) {
      return (StringUtils.isBlank(textLeft) && countRight >= 1) || (countRight > countLeft && !textRight.equals(textLeft));
   }

   static boolean helperUpdateGoldenRecordField(
         final LibMPI libMPI,
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
         if (!freqMapGroupedByField.isEmpty()) {
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
            }
         }
      }
      return changed;
   }

   static void helperUpdateInteractionsScore(
         final LibMPI libMPI,
         final float threshold,
         final ExpandedGoldenRecord expandedGoldenRecord) {
      expandedGoldenRecord.interactionsWithScore().forEach(interactionWithScore -> {
         final var interaction = interactionWithScore.interaction();
         final var score =
               LinkerUtils.calcNormalizedScore(expandedGoldenRecord.goldenRecord().demographicData(),
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
                                AppUtils.getNames(interaction.demographicData()),
                                new Notification.MatchData(expandedGoldenRecord.goldenRecord().goldenId(), score),
                                List.of());
            }
         }
      });
   }
// +
   public static Either<LinkInfo, List<ExternalLinkCandidate>> linkInteraction(
         final LibMPI libMPI,
         final Interaction interaction,
         final ExternalLinkRange externalLinkRange,
         final float matchThreshold_, final String envelopeStan) {

      InteractionProcessorConnector interactionProcessorConnector = new InteractionProcessorConnector(AppConfig.KAFKA_BOOTSTRAP_SERVERS);

      interactionProcessorConnector.sendOnNewNotification(interaction, envelopeStan);
      if (!CustomLinkerDeterministic.canApplyLinking(interaction.demographicData())) {
         libMPI.startTransaction();
         if (CustomLinkerDeterministic.DETERMINISTIC_DO_MATCHING || CustomLinkerProbabilistic.PROBABILISTIC_DO_MATCHING) {
            final var candidates = libMPI.findMatchCandidates(interaction.demographicData());
            LOGGER.debug("Match Candidates {} ", candidates.size());
            if (candidates.isEmpty()) {
               try {
                  final var i = OBJECT_MAPPER.writeValueAsString(interaction.demographicData());
                  final var f =
                        """
                        MATCH NOTIFICATION NO CANDIDATE
                        {}""";
                  LOGGER.info(f, i);
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            } else {
               final var workCandidate =
                     candidates.parallelStream()
                               .unordered()
                               .map(candidate -> new WorkCandidate(candidate,
                                                                   LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                                                                                   interaction.demographicData())))
                               .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                               .collect(Collectors.toCollection(ArrayList::new))
                               .get(0);
               try {
                  final var i = OBJECT_MAPPER.writeValueAsString(interaction.demographicData());
                  final var g = OBJECT_MAPPER.writeValueAsString(workCandidate.goldenRecord().demographicData());
                  final var f =
                        """
                        MATCH NOTIFICATION
                        {}
                        {}""";
                  LOGGER.info(f, i, g);
               } catch (JsonProcessingException e) {
                  LOGGER.error(e.getLocalizedMessage(), e);
               }
            }
         }
         libMPI.closeTransaction();
         return Either.right(List.of());
      } else {
         LinkInfo linkInfo = null;
         final List<ExternalLinkCandidate> externalLinkCandidateList = new ArrayList<>();
         final var matchThreshold = externalLinkRange != null
               ? externalLinkRange.high()
               : matchThreshold_;


         try {
            libMPI.startTransaction();
            LinkerProbabilistic.checkUpdatedMU();

            final var candidateGoldenRecords = libMPI.findLinkCandidates(interaction.demographicData());

            if (candidateGoldenRecords.isEmpty()) {
               linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
            } else {

               Map<String, Float> candidatesIdsWithScores = new HashMap<>();
               final var allCandidateScores =
                     candidateGoldenRecords.parallelStream()
                                           .unordered()
                                           .map(candidate -> new WorkCandidate(candidate,
                                                                               LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                                                                                               interaction.demographicData())))
                                           .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                                           .peek(w -> candidatesIdsWithScores.put(w.goldenRecord().goldenId(), w.score()))
                                           .collect(Collectors.toCollection(ArrayList::new));

               interactionProcessorConnector.sendOnProcessCandidates(interaction, envelopeStan, matchThreshold, candidatesIdsWithScores);
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
                                 belowThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(),
                                                                                            v.score()));
                              } else if (v.score() >= matchThreshold && v.score() < matchThreshold + 0.1) {
                                 aboveThresholdNotifications.add(new Notification.MatchData(v.goldenRecord().goldenId(),
                                                                                            v.score()));
                              }
                           })
                           .filter(v -> v.score() >= matchThreshold)
                           .collect(Collectors.toCollection(ArrayList::new));

               if (candidatesAboveMatchThreshold.isEmpty()) {
                  if (candidatesInExternalLinkRange.isEmpty()) {
                     linkInfo = libMPI.createInteractionAndLinkToClonedGoldenRecord(interaction, 1.0F);
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
                  final var validated1 =
                        CustomLinkerDeterministic.validateDeterministicMatch(firstCandidate.goldenRecord.demographicData(),
                                                                             interaction.demographicData());
                  final var validated2 =
                        CustomLinkerProbabilistic.validateProbabilisticScore(firstCandidate.goldenRecord.demographicData(),
                                                                             interaction.demographicData());
                  linkInfo =
                        libMPI.createInteractionAndLinkToExistingGoldenRecord(interaction,
                                                                              linkToGoldenId,
                                                                              validated1,
                                                                              validated2);

                  if (linkToGoldenId.score() <= matchThreshold + 0.1) {
                     sendNotification(Notification.NotificationType.THRESHOLD,
                                      linkInfo.interactionUID(),
                                      AppUtils.getNames(interaction.demographicData()),
                                      new Notification.MatchData(linkInfo.goldenUID(), linkInfo.score()),
                                      aboveThresholdNotifications.stream().filter(m -> !Objects.equals(m.gID(), firstCandidate.goldenRecord.goldenId())).collect(Collectors.toCollection(ArrayList::new)));
                  }

                  if (Boolean.TRUE.equals(firstCandidate.goldenRecord.customUniqueGoldenRecordData().auxAutoUpdateEnabled())) {
                     CustomLinkerBackEnd.updateGoldenRecordFields(libMPI,
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

   public record WorkCandidate(
         GoldenRecord goldenRecord,
         float score) {
   }

}
