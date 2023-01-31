package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

import static java.lang.Math.log;

public class CustomLinkerProbabilistic {

   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerProbabilistic.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
   private static final double LOG2 = java.lang.Math.log(2.0);
   private static Fields updatedFields = null;

   private CustomLinkerProbabilistic() {}

   private static float limitProbability(final float p) {
      if (p > 1.0F - 1E-5F) {
         return 1.0F - 1E-5F;
      } else if (p < 1E-5F) {
         return 1E-5F;
      }
      return p;
   }

   private static float fieldScore(final boolean match, final float m, final float u) {
      if (match) {
         return (float) (log(m / u) / LOG2);
      }
      return (float) (log((1.0 - m) / (1.0 - u)) / LOG2);
   }

   private static float fieldScore(final String left, final String right, final Field field) {
      return fieldScore(JARO_WINKLER_SIMILARITY.apply(left, right) > 0.92, field.m, field.u);
   }

   private static CustomMU.Probability getProbability(final Field field) {
      return new CustomMU.Probability(field.m(), field.u());
   }

   public static void checkUpdatedMU() {
      if (updatedFields != null) {
         LOGGER.info("Using updated MU values: {}", updatedFields);
         currentFields = updatedFields;
         updatedFields = null;
      }
   }

   private record Field(float m, float u, float min, float max) {
      Field {
         m = limitProbability(m);
         u = limitProbability(u);
         min = fieldScore(false, m, u);
         max = fieldScore(true, m, u);
      }

      Field(final float m, final float u) {
         this(m, u, 0.0F, 0.0F);
      }

   }

   private static void updateMetricsForStringField(final float[] metrics,
                                                   final String left, final String right,
                                                   final Field field) {
      final float MISSING_PENALTY = 0.925F;
      if (StringUtils.isNotBlank(left) && StringUtils.isNotBlank(right)) {
         metrics[0] += field.min;
         metrics[1] += field.max;
         metrics[2] += fieldScore(left, right, field);
      } else {
         metrics[3] *= MISSING_PENALTY;
      }
   }

   static CustomMU getMU() {
      return new CustomMU(
         getProbability(currentFields.givenName),
         getProbability(currentFields.familyName),
         getProbability(currentFields.gender),
         getProbability(currentFields.dob),
         getProbability(currentFields.city),
         getProbability(currentFields.phoneNumber),
         getProbability(currentFields.nationalId));
   }

   private record Fields(Field givenName,
          Field familyName,
          Field gender,
          Field dob,
          Field city,
          Field phoneNumber,
          Field nationalId) {}

   private static Fields currentFields =
      new Fields(new Field(0.782501F, 0.02372F),
                 new Field(0.850909F, 0.02975F),
                 new Field(0.786614F, 0.443018F),
                 new Field(0.894637F, 0.012448F),
                 new Field(0.872691F, 0.132717F),
                 new Field(0.920281F, 0.322629F),
                 new Field(0.832336F, 1.33E-4F));

   public static float probabilisticScore(final CustomGoldenRecord goldenRecord, final CustomEntity
   customEntity) {
      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};
      updateMetricsForStringField(metrics,
                                  goldenRecord.givenName(), customEntity.givenName(), currentFields.givenName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.familyName(), customEntity.familyName(), currentFields.familyName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.gender(), customEntity.gender(), currentFields.gender);
      updateMetricsForStringField(metrics,
                                  goldenRecord.dob(), customEntity.dob(), currentFields.dob);
      updateMetricsForStringField(metrics,
                                  goldenRecord.city(), customEntity.city(), currentFields.city);
      updateMetricsForStringField(metrics,
                                  goldenRecord.phoneNumber(), customEntity.phoneNumber(), currentFields.phoneNumber);
      updateMetricsForStringField(metrics,
                                  goldenRecord.nationalId(), customEntity.nationalId(), currentFields.nationalId);
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
            new Field(mu.givenName().m(), mu.givenName().u()),
            new Field(mu.familyName().m(), mu.familyName().u()),
            new Field(mu.gender().m(), mu.gender().u()),
            new Field(mu.dob().m(), mu.dob().u()),
            new Field(mu.city().m(), mu.city().u()),
            new Field(mu.phoneNumber().m(), mu.phoneNumber().u()),
            new Field(mu.nationalId().m(), mu.nationalId().u()));
      }
   }

}
