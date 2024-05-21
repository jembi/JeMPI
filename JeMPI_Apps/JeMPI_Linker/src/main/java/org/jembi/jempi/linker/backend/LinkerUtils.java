package org.jembi.jempi.linker.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.LinkingRule;

public final class LinkerUtils {

   private static final Logger LOGGER = LogManager.getLogger(LinkerUtils.class);

   private LinkerUtils() {
   }

   public static float calcNormalizedScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      if (CustomLinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }
      return CustomLinkerProbabilistic.linkProbabilisticScore(goldenRecord, interaction);
   }

   public static LinkingRule determineLinkingRule(
            final CustomDemographicData goldenRecord,
            final CustomDemographicData interaction) {
        if (CustomLinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
            return LinkingRule.DETERMINISTIC;
        }
        return LinkingRule.PROBABILISTIC;
   }

}
