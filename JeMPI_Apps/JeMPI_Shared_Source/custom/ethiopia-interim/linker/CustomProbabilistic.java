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

   private static final Logger LOGGER = LogManager.getLogger(CustomProbabilistic.class);
   private static final JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();
   private static final double LOG2 = log(2.0);
   static Fields currentFields =
         new Fields(new Field(0.772219228755136F, 0.06655090551872511F, 0, 0),
                    new Field(0.8546874441576465F, 0.08914844028670499F, 0, 0),
                    new Field(0.8546874441576465F, 0.08914844028670499F, 0, 0),
                    new Field(0.8546874441576465F, 0.08914844028670499F, 0, 0),
                    new Field(0.8546874441576465F, 0.08914844028670499F, 0, 0),
                    new Field(0.9999999999999986F, 0.5330203920075635F, 0, 0),
                    new Field(0.9750650828752208F, 0.01054543218263664F, 0, 0),
                    new Field(0.9991452852785773F, 0.8224363243459457F, 0, 0),
                    new Field(0.9865865930557971F, 0.0038466177390885903F, 0, 0));
   private static Fields updatedFields = null;

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

   public static void updateMU(final CustomMU mu) {
      if (mu.nameGiven().m() > mu.nameGiven().u()
          && mu.nameFather().m() > mu.nameFather().u()
          && mu.nameFathersFather().m() > mu.nameFathersFather().u()
          && mu.nameMother().m() > mu.nameMother().u()
          && mu.nameMothersFather().m() > mu.nameMothersFather().u()
          && mu.gender().m() > mu.gender().u()
          && mu.dob().m() > mu.dob().u()
          && mu.city().m() > mu.city().u()
          && mu.phoneNumber().m() > mu.phoneNumber().u()) {
         updatedFields = new Fields(
               new Field(mu.nameGiven().m(), mu.nameGiven().u(), 0, 0),
               new Field(mu.nameFather().m(), mu.nameFather().u(), 0, 0),
               new Field(mu.nameFathersFather().m(), mu.nameFathersFather().u(), 0, 0),
               new Field(mu.nameMother().m(), mu.nameMother().u(), 0, 0),
               new Field(mu.nameMothersFather().m(), mu.nameMothersFather().u(), 0, 0),
               new Field(mu.gender().m(), mu.gender().u(), 0, 0),
               new Field(mu.dob().m(), mu.dob().u(), 0, 0),
               new Field(mu.city().m(), mu.city().u(), 0, 0),
               new Field(mu.phoneNumber().m(), mu.phoneNumber().u(), 0, 0));
      }
   }

   private static void updateMetricsForStringField(final float[] metrics, final String left, final String right,
                                                   final Field field) {
      final float MISSING_PENALTY = 0.925F;
      if (StringUtils.isNotEmpty(left) && StringUtils.isNotEmpty(right)) {
         metrics[0] += field.min;
         metrics[1] += field.max;
         metrics[2] += fieldScore(left, right, field);
      } else {
         metrics[3] *= MISSING_PENALTY;
      }
   }

   public static float probabilisticScore(final MpiGoldenRecord goldenRecord, final MpiDocument doc) {

      // min, max, score, missingPenalty
      final float[] metrics = {0, 0, 0, 1.0F};

      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nameGiven(), doc.entity().nameGiven(), currentFields.givenName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nameFather(), doc.entity().nameFather(), currentFields.fathersName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nameFathersFather(), doc.entity().nameFathersFather(),
                                  currentFields.fathersFatherName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nameMother(), doc.entity().nameMother(), currentFields.mothersName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().nameMothersFather(), doc.entity().nameMothersFather(),
                                  currentFields.motherFatherName);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().gender(), doc.entity().gender(), currentFields.gender);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().dob(), doc.entity().dob(), currentFields.dob);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().city(), doc.entity().city(), currentFields.city);
      updateMetricsForStringField(metrics,
                                  goldenRecord.entity().phoneNumber(), doc.entity().phoneNumber(), currentFields.phoneNumber);

      return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];
   }

   public static void checkUpdatedMU() {
      if (updatedFields != null) {
         LOGGER.info("Using updated MU values: {}", updatedFields);
         CustomProbabilistic.currentFields = updatedFields;
         updatedFields = null;
      }
   }

   public static CustomMU getMU() {
      return new CustomMU(getProbability(currentFields.givenName),
                          getProbability(currentFields.fathersName),
                          getProbability(currentFields.fathersFatherName),
                          getProbability(currentFields.mothersName),
                          getProbability(currentFields.motherFatherName),
                          getProbability(currentFields.gender),
                          getProbability(currentFields.dob),
                          getProbability(currentFields.city),
                          getProbability(currentFields.phoneNumber));
   }

   record Fields(Field givenName,
                 Field fathersName,
                 Field fathersFatherName,
                 Field mothersName,
                 Field motherFatherName,
                 Field gender,
                 Field dob,
                 Field city,
                 Field phoneNumber) {}

   private record Field(float m, float u, float min, float max) {
      Field {
         m = limitProbability(m);
         u = limitProbability(u);
         min = fieldScore(false, m, u);
         max = fieldScore(true, m, u);
      }
   }

}
