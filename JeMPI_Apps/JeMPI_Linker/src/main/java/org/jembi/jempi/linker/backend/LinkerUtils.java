package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.models.CustomDemographicData;

final class LinkerUtils {

   private LinkerUtils() {
   }

   static float calcNormalizedScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      if (CustomLinkerDeterministic.linkDeterministicMatch(goldenRecord, interaction)) {
         return 1.0F;
      }

      return CustomLinkerProbabilistic.linkProbabilisticScore(goldenRecord, interaction);
   }
}
