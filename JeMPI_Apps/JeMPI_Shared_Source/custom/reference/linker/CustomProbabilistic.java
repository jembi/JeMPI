DEPRECATED

package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.models.MpiDocument;
import org.jembi.jempi.libmpi.models.MpiGoldenRecord;
import org.jembi.jempi.shared.models.CustomMU;

import static java.lang.Math.log;

public final class CustomProbabilistic {

/*
   private static final Logger LOGGER = LogManager.getLogger(CustomProbabilistic.class);
   private static final JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();
   private static final double LOG2 = java.lang.Math.log(2.0);
   private static Fields updatedFields = null;

   private static Fields currentFields =
         new Fields(new Field(0.772219228755136F, 0.06655090551872511F, 0, 0),
                    new Field(0.8546874441576465F, 0.08914844028670499F, 0, 0),
                    new Field(0.9999999999999986F, 0.5330203920075635F, 0, 0),
                    new Field(0.9750650828752208F, 0.01054543218263664F, 0, 0),
                    new Field(0.9991452852785773F, 0.8224363243459457F, 0, 0),
                    new Field(0.9865865930557971F, 0.0038466177390885903F, 0, 0),
                    new Field(0.9722297419718188F, 6.097993328125237E-5F, 0, 0));

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
      return fieldScore(JARO_WINKLER_DISTANCE.apply(left, right) > 0.92, field.m, field.u);
   }

   private static CustomMU.Probability getProbability(final Field field) {
      return new CustomMU.Probability(field.m(), field.u());
   }

   static CustomMU getMU() {
      return new CustomMU(getProbability(currentFields.givenName),
                          getProbability(currentFields.familyName),
                          getProbability(currentFields.gender),
                          getProbability(currentFields.dob),
                          getProbability(currentFields.city),
                          getProbability(currentFields.phoneNumber),
                          getProbability(currentFields.nationalId));
   }

   public static void updateMU(final CustomMU mu) {
      if (mu.givenName().m() > mu.givenName().u()
          && mu.familyName().m() > mu.familyName().u()
          && mu.gender().m() > mu.gender().u()
          && mu.dob().m() > mu.dob().u()
          && mu.city().m() > mu.city().u()
          && mu.phoneNumber().m() > mu.phoneNumber().u()
          && mu.nationalId().m() > mu.nationalId().u()) {
         updatedFields = new Fields(new Field(mu.givenName().m(), mu.givenName().u(), 0, 0),
                                    new Field(mu.familyName().m(), mu.familyName().u(), 0, 0),
                                    new Field(mu.gender().m(), mu.gender().u(), 0, 0),
                                    new Field(mu.dob().m(), mu.dob().u(), 0, 0),
                                    new Field(mu.city().m(), mu.city().u(), 0, 0),
                                    new Field(mu.phoneNumber().m(), mu.phoneNumber().u(), 0, 0),
                                    new Field(mu.nationalId().m(), mu.nationalId().u(), 0, 0));
      }
   }

   public static float probabilisticScore(final MpiGoldenRecord goldenRecord, final MpiDocument doc) {

      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};

      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().givenName(), doc.entity().givenName(), currentFields.givenName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().familyName(), doc.entity().familyName(), currentFields.familyName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().gender(), doc.entity().gender(), currentFields.gender);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().dob(), doc.entity().dob(), currentFields.dob);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().city(), doc.entity().city(), currentFields.city);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().phoneNumber(), doc.entity().phoneNumber(), currentFields.phoneNumber);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nationalId(), doc.entity().nationalId(), currentFields.nationalId);
      return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
   }

   private static void updateMetricsForStringField(final float[] metrics, final String left, final String right,
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

   public static void checkUpdatedMU() {
      if (updatedFields != null) {
         LOGGER.info("Using updated MU values: {}", updatedFields);
         CustomProbabilistic.currentFields = updatedFields;
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
   }

   private record Fields(Field givenName,
                         Field familyName,
                         Field gender,
                         Field dob,
                         Field city,
                         Field phoneNumber,
                         Field nationalId) {}

*/
}
