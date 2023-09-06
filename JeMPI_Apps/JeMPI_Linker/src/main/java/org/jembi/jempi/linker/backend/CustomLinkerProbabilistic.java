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

   static LinkFields updatedFields = null;

   private CustomLinkerProbabilistic() {
   }

   static CustomMU getMU() {
      return new CustomMU(
         LinkerProbabilistic.getProbability(currentLinkFields.nationalId));
   }

   private record LinkFields(
         LinkerProbabilistic.Field nationalId) {
   }

   private record ValidateFields(
         LinkerProbabilistic.Field givenName,
         LinkerProbabilistic.Field familyName,
         LinkerProbabilistic.Field gender,
         LinkerProbabilistic.Field dob,
         LinkerProbabilistic.Field city,
         LinkerProbabilistic.Field phoneNumber) {
   }

   static LinkFields currentLinkFields =
      new LinkFields(
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8441029F, 2.0E-7F));

   static ValidateFields currentValidateFields =
      new ValidateFields(
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8806329F, 0.0026558F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9140443F, 6.275E-4F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9468393F, 0.4436446F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.7856196F, 4.65E-5F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8445694F, 0.0355741F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.84085F, 4.0E-7F));

   static float linkProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.nationalId, interaction.nationalId, currentLinkFields.nationalId);
      return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
   }

   static float validateProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.givenName, interaction.givenName, currentValidateFields.givenName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.familyName, interaction.familyName, currentValidateFields.familyName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.gender, interaction.gender, currentValidateFields.gender);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.dob, interaction.dob, currentValidateFields.dob);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.city, interaction.city, currentValidateFields.city);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.phoneNumber, interaction.phoneNumber, currentValidateFields.phoneNumber);
      return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
   }

   public static void updateMU(final CustomMU mu) {
      if (mu.nationalId().m() > mu.nationalId().u()) {
         updatedFields = new LinkFields(
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.nationalId().m(), mu.nationalId().u()));
      }
   }

}
