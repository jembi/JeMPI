package org.jembi.jempi.linker.backend;

import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.Arrays;
import java.util.List;

import static org.jembi.jempi.linker.backend.LinkerProbabilistic.EXACT_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JACCARD_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_WINKLER_SIMILARITY;

final class CustomLinkerProbabilistic {

   static final int METRIC_MIN = 0;
   static final int METRIC_MAX = 1;
   static final int METRIC_SCORE = 2;
   static final int METRIC_MISSING_PENALTY = 3;
   static final boolean PROBABILISTIC_DO_LINKING = false;
   static final boolean PROBABILISTIC_DO_VALIDATING = false;
   static final boolean PROBABILISTIC_DO_MATCHING = false;





   private CustomLinkerProbabilistic() {
   }

   static float linkProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      return 0.0F;
   }

   static float validateProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      return 0.0F;
   }

   static float matchNotificationProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      return 0.0F;
   }
   public static void updateMU(final CustomMU mu) {
   }

}
