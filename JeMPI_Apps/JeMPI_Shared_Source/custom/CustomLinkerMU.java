package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;

public class CustomLinkerMU {

   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerMU.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();

   private final Fields fields = new Fields();

   CustomLinkerMU() {
      LOGGER.debug("CustomLinkerMU");
   }

   private static boolean fieldMismatch(final String left, final String right) {
      return JARO_WINKLER_SIMILARITY.apply(left, right) <= 0.92;
   }

   private void updateMatchedPair(final Field field, final String left, final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
         field.matchedPairFieldUnmatched += 1;
      } else {
         field.matchedPairFieldMatched += 1;
      }
   }

   private void updateUnMatchedPair(final Field field, final String left, final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(left, right)) {
         field.unMatchedPairFieldUnmatched += 1;
      } else {
         field.unMatchedPairFieldMatched += 1;
      }
   }

   void updateMatchSums(final CustomEntity customEntity, final CustomGoldenRecord customGoldenRecord) {
      updateMatchedPair(fields.givenName, customEntity.givenName(), customGoldenRecord.givenName());
      updateMatchedPair(fields.familyName, customEntity.familyName(), customGoldenRecord.familyName());
      updateMatchedPair(fields.gender, customEntity.gender(), customGoldenRecord.gender());
      updateMatchedPair(fields.dob, customEntity.dob(), customGoldenRecord.dob());
      updateMatchedPair(fields.city, customEntity.city(), customGoldenRecord.city());
      updateMatchedPair(fields.phoneNumber, customEntity.phoneNumber(), customGoldenRecord.phoneNumber());
      updateMatchedPair(fields.nationalId, customEntity.nationalId(), customGoldenRecord.nationalId());
      LOGGER.debug("{}", fields);
   }

   void updateMissmatchSums(final CustomEntity customEntity, final CustomGoldenRecord customGoldenRecord) {
      updateUnMatchedPair(fields.givenName, customEntity.givenName(), customGoldenRecord.givenName());
      updateUnMatchedPair(fields.familyName, customEntity.familyName(), customGoldenRecord.familyName());
      updateUnMatchedPair(fields.gender, customEntity.gender(), customGoldenRecord.gender());
      updateUnMatchedPair(fields.dob, customEntity.dob(), customGoldenRecord.dob());
      updateUnMatchedPair(fields.city, customEntity.city(), customGoldenRecord.city());
      updateUnMatchedPair(fields.phoneNumber, customEntity.phoneNumber(), customGoldenRecord.phoneNumber());
      updateUnMatchedPair(fields.nationalId, customEntity.nationalId(), customGoldenRecord.nationalId());
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