package org.jembi.jempi.linker;

import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.Arrays;
import java.util.List;

import static org.jembi.jempi.linker.LinkerProbabilistic.EXACT_SIMILARITY;
import static org.jembi.jempi.linker.LinkerProbabilistic.JARO_WINKLER_SIMILARITY;

final class CustomLinkerProbabilistic {

   static Fields updatedFields = null;

   private CustomLinkerProbabilistic() {
   }

   static CustomMU getMU() {
      return new CustomMU(
         LinkerProbabilistic.getProbability(currentFields.givenName),
         LinkerProbabilistic.getProbability(currentFields.familyName),
         LinkerProbabilistic.getProbability(currentFields.gender),
         LinkerProbabilistic.getProbability(currentFields.dob),
         LinkerProbabilistic.getProbability(currentFields.city),
         LinkerProbabilistic.getProbability(currentFields.phoneNumber),
         LinkerProbabilistic.getProbability(currentFields.nationalId));
   }

   private record Fields(
         LinkerProbabilistic.Field givenName,
         LinkerProbabilistic.Field familyName,
         LinkerProbabilistic.Field gender,
         LinkerProbabilistic.Field dob,
         LinkerProbabilistic.Field city,
         LinkerProbabilistic.Field phoneNumber,
         LinkerProbabilistic.Field nationalId) {
   }

   static Fields currentFields =
      new Fields(new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.782501F, 0.02372F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.850909F, 0.02975F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.786614F, 0.443018F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.894637F, 0.012448F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.872691F, 0.132717F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.920281F, 0.322629F),
                 new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, Arrays.asList(0.93F, 0.92F, 0.91F, 0.9F), 0.832336F, 1.33E-4F));

   public static float probabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.givenName, interaction.givenName, currentFields.givenName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.familyName, interaction.familyName, currentFields.familyName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.gender, interaction.gender, currentFields.gender);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.dob, interaction.dob, currentFields.dob);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.city, interaction.city, currentFields.city);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.phoneNumber, interaction.phoneNumber, currentFields.phoneNumber);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.nationalId, interaction.nationalId, currentFields.nationalId);
      return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
   }

   public static void updateMU(final CustomMU mu) {
      if (mu.givenName().m() > mu.givenName().u()
          && mu.familyName().m() > mu.familyName().u()
          && mu.gender().m() > mu.gender().u()
          && mu.dob().m() > mu.dob().u()
          && mu.city().m() > mu.city().u()
          && mu.phoneNumber().m() > mu.phoneNumber().u()
          && mu.nationalId().m() > mu.nationalId().u()) {
         updatedFields = new Fields(
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.givenName().m(), mu.givenName().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.familyName().m(), mu.familyName().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.gender().m(), mu.gender().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.dob().m(), mu.dob().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.city().m(), mu.city().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.phoneNumber().m(), mu.phoneNumber().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, Arrays.asList(0.93F, 0.92F, 0.91F, 0.9F), mu.nationalId().m(), mu.nationalId().u()));
      }
   }

}
