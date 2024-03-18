package org.jembi.jempi.linker.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomMU;

import java.util.Arrays;
import java.util.List;

import static org.jembi.jempi.linker.backend.LinkerProbabilistic.EXACT_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JACCARD_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_SIMILARITY;
import static org.jembi.jempi.linker.backend.LinkerProbabilistic.JARO_WINKLER_SIMILARITY;
import static org.jembi.jempi.shared.models.CustomDemographicData.*;

final class CustomLinkerProbabilistic {

   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerProbabilistic.class);
   static final int METRIC_MIN = 0;
   static final int METRIC_MAX = 1;
   static final int METRIC_SCORE = 2;
   static final int METRIC_MISSING_PENALTY = 3;
   static final boolean PROBABILISTIC_DO_LINKING = true;
   static final boolean PROBABILISTIC_DO_VALIDATING = false;
   static final boolean PROBABILISTIC_DO_MATCHING = false;

   static LinkFields updatedLinkFields = null;



   private CustomLinkerProbabilistic() {
   }

   private record LinkFields(
         LinkerProbabilistic.Field givenName,
         LinkerProbabilistic.Field familyName,
         LinkerProbabilistic.Field gender,
         LinkerProbabilistic.Field dob,
         LinkerProbabilistic.Field city,
         LinkerProbabilistic.Field phoneNumber,
         LinkerProbabilistic.Field nationalId) {
   }

   static LinkFields currentLinkFields =
      new LinkFields(
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8806329F, 0.0026558F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9140443F, 6.275E-4F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9468393F, 0.4436446F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.7856196F, 4.65E-5F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8445694F, 0.0355741F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.84085F, 4.0E-7F),
         new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8441029F, 2.0E-7F));

   static float linkProbabilisticScore(
         final CustomDemographicData goldenRecord,
         final CustomDemographicData interaction) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(GIVEN_NAME).value(), interaction.fields.get(GIVEN_NAME).value(), currentLinkFields.givenName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(FAMILY_NAME).value(), interaction.fields.get(FAMILY_NAME).value(), currentLinkFields.familyName);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(GENDER).value(), interaction.fields.get(GENDER).value(), currentLinkFields.gender);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(DOB).value(), interaction.fields.get(DOB).value(), currentLinkFields.dob);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(CITY).value(), interaction.fields.get(CITY).value(), currentLinkFields.city);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(PHONE_NUMBER).value(), interaction.fields.get(PHONE_NUMBER).value(), currentLinkFields.phoneNumber);
      LinkerProbabilistic.updateMetricsForStringField(metrics,
                                                      goldenRecord.fields.get(NATIONAL_ID).value(), interaction.fields.get(NATIONAL_ID).value(), currentLinkFields.nationalId);
      return ((metrics[METRIC_SCORE] - metrics[METRIC_MIN]) / (metrics[METRIC_MAX] - metrics[METRIC_MIN])) * metrics[METRIC_MISSING_PENALTY];

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
      if (mu.customLinkMU().givenName().m() > mu.customLinkMU().givenName().u()
          && mu.customLinkMU().familyName().m() > mu.customLinkMU().familyName().u()
          && mu.customLinkMU().gender().m() > mu.customLinkMU().gender().u()
          && mu.customLinkMU().dob().m() > mu.customLinkMU().dob().u()
          && mu.customLinkMU().city().m() > mu.customLinkMU().city().u()
          && mu.customLinkMU().phoneNumber().m() > mu.customLinkMU().phoneNumber().u()
          && mu.customLinkMU().nationalId().m() > mu.customLinkMU().nationalId().u()) {
         updatedLinkFields = new LinkFields(
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().givenName().m(), mu.customLinkMU().givenName().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().familyName().m(), mu.customLinkMU().familyName().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().gender().m(), mu.customLinkMU().gender().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().dob().m(), mu.customLinkMU().dob().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().city().m(), mu.customLinkMU().city().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().phoneNumber().m(), mu.customLinkMU().phoneNumber().u()),
            new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), mu.customLinkMU().nationalId().m(), mu.customLinkMU().nationalId().u()));
      }
   }

   public static void checkUpdatedLinkMU() {
      if (updatedLinkFields != null) {
         LOGGER.info("Using updated Link MU values: {}", updatedLinkFields);
         CustomLinkerProbabilistic.currentLinkFields = updatedLinkFields;
         updatedLinkFields = null;
     }

   }

}
