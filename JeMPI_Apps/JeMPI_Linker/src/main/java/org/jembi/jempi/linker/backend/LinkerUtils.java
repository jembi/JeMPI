package org.jembi.jempi.linker.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.LinkingRule;

public final class LinkerUtils {

   private static final Logger LOGGER = LogManager.getLogger(LinkerUtils.class);

   private LinkerUtils() {
   }

   public static float calcNormalizedMatchScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      if (LinkerDeterministic.matchNotificationDeterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }
      return LinkerProbabilistic.matchProbabilisticScore(goldenRecord, interaction);
   }

   public static float calcNormalizedValidateScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      if (LinkerDeterministic.validateDeterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }
      return LinkerProbabilistic.validateProbabilisticScore(goldenRecord, interaction);
   }

   public static float calcNormalizedLinkScore(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      if (LinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }
      return LinkerProbabilistic.linkProbabilisticScore(goldenRecord, interaction);
   }

   public static LinkingRule determineLinkRule(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      if (LinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
         return LinkingRule.DETERMINISTIC;
      }
      return LinkingRule.PROBABILISTIC;
   }

   public static LinkingRule determineMatchRule(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      if (LinkerDeterministic.matchNotificationDeterministicMatch(goldenRecord, interaction)) {
         return LinkingRule.DETERMINISTIC;
      }
      return LinkingRule.PROBABILISTIC;
   }

}
