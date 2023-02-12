package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;


public class CustomLinkerMU {

   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerMU.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();

   private final Fields fields = new Fields();

   CustomLinkerMU() {
      LOGGER.debug("CustomLinkerMU");
   }

   private static boolean fieldMismatch(
         final String left,
         final String right) {
      return JARO_WINKLER_SIMILARITY.apply(left, right) <= 0.92;
   }

   private void updateMatchedPair(
         final Field field,
         final String left,
         final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
         field.matchedPairFieldUnmatched += 1;
      } else {
         field.matchedPairFieldMatched += 1;
      }
   }

   private void updateUnMatchedPair(
         final Field field,
         final String left,
         final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
         field.unMatchedPairFieldUnmatched += 1;
      } else {
         field.unMatchedPairFieldMatched += 1;
      }
   }

   void updateMatchSums(
         final CustomDemographicData patient,
         final CustomDemographicData customGoldenRecord) {
      updateMatchedPair(fields.givenName, patient.givenName(), customGoldenRecord.givenName());
      updateMatchedPair(fields.familyName, patient.familyName(), customGoldenRecord.familyName());
      updateMatchedPair(fields.gender, patient.gender(), customGoldenRecord.gender());
      updateMatchedPair(fields.dob, patient.dob(), customGoldenRecord.dob());
      updateMatchedPair(fields.city, patient.city(), customGoldenRecord.city());
      updateMatchedPair(fields.phoneNumber, patient.phoneNumber(), customGoldenRecord.phoneNumber());
      updateMatchedPair(fields.nationalId, patient.nationalId(), customGoldenRecord.nationalId());
      LOGGER.debug("{}", fields);
   }

   void updateMissmatchSums(
         final CustomDemographicData patient,
         final CustomDemographicData customGoldenRecord) {
      updateUnMatchedPair(fields.givenName, patient.givenName(), customGoldenRecord.givenName());
      updateUnMatchedPair(fields.familyName, patient.familyName(), customGoldenRecord.familyName());
      updateUnMatchedPair(fields.gender, patient.gender(), customGoldenRecord.gender());
      updateUnMatchedPair(fields.dob, patient.dob(), customGoldenRecord.dob());
      updateUnMatchedPair(fields.city, patient.city(), customGoldenRecord.city());
      updateUnMatchedPair(fields.phoneNumber, patient.phoneNumber(), customGoldenRecord.phoneNumber());
      updateUnMatchedPair(fields.nationalId, patient.nationalId(), customGoldenRecord.nationalId());
      LOGGER.debug("{}", fields);
   }

   static class Field {
      long matchedPairFieldMatched = 0L;
      long matchedPairFieldUnmatched = 0L;
      long unMatchedPairFieldMatched = 0L;
      long unMatchedPairFieldUnmatched = 0L;
   }

   static class Fields {
      final Field givenName = new Field();
      final Field familyName = new Field();
      final Field gender = new Field();
      final Field dob = new Field();
      final Field city = new Field();
      final Field phoneNumber = new Field();
      final Field nationalId = new Field();

      private float computeM(Field field) {
         return (float) (field.matchedPairFieldMatched)
                / (float) (field.matchedPairFieldMatched + field.matchedPairFieldUnmatched);
      }

      private float computeU(Field field) {
         return (float) (field.unMatchedPairFieldMatched)
                / (float) (field.unMatchedPairFieldMatched + field.unMatchedPairFieldUnmatched);
      }

      @Override
      public String toString() {
         return String.format("f1(%f:%f) f2(%f:%f) f3(%f:%f) f4(%f:%f) f5(%f:%f) f6(%f:%f) f7(%f:%f)",
                              computeM(givenName), computeU(givenName),
                              computeM(familyName), computeU(familyName),
                              computeM(gender), computeU(gender),
                              computeM(dob), computeU(dob),
                              computeM(city), computeU(city),
                              computeM(phoneNumber), computeU(phoneNumber),
                              computeM(nationalId), computeU(nationalId));
      }

   }

}