package org.jembi.jempi.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;


public final class CustomLinkerMU {

   private static final Logger LOGGER = LogManager.getLogger(CustomLinkerMU.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();

   private final Fields fields = new Fields();

   CustomLinkerMU() {
      LOGGER.debug("CustomLinkerMU");
   }

   private static boolean fieldMismatch(
         final Field field,
         final String left,
         final String right) {
      return field.similarityScore.apply(left, right) <= field.threshold;
   }

   private void updateMatchedPair(
         final Field field,
         final String left,
         final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(field, left, right)) {
         field.matchedPairFieldUnmatched += 1;
      } else {
         field.matchedPairFieldMatched += 1;
      }
   }

   private void updateUnMatchedPair(
         final Field field,
         final String left,
         final String right) {
      if (StringUtils.isBlank(left) || StringUtils.isBlank(right) || fieldMismatch(field, left, right)) {
         field.unMatchedPairFieldUnmatched += 1;
      } else {
         field.unMatchedPairFieldMatched += 1;
      }
   }

   void updateMatchSums(
         final CustomDemographicData patient,
         final CustomDemographicData goldenRecord) {
      updateMatchedPair(fields.nationalId, patient.nationalId, goldenRecord.nationalId);
      LOGGER.debug("{}", fields);
   }

   void updateMissmatchSums(
         final CustomDemographicData patient,
         final CustomDemographicData goldenRecord) {
      updateUnMatchedPair(fields.nationalId, patient.nationalId, goldenRecord.nationalId);
      LOGGER.debug("{}", fields);
   }

   static class Field {
      final SimilarityScore<Double> similarityScore;
      final double threshold;
      long matchedPairFieldMatched = 0L;
      long matchedPairFieldUnmatched = 0L;
      long unMatchedPairFieldMatched = 0L;
      long unMatchedPairFieldUnmatched = 0L;

      Field(final SimilarityScore<Double> score,
            final double mismatchThreshold) {
         this.similarityScore = score;
         this.threshold = mismatchThreshold;
      }
   }

   static class Fields {
      final Field nationalId = new Field(JARO_WINKLER_SIMILARITY, 0.92);

      private float computeM(final Field field) {
         return (float) (field.matchedPairFieldMatched)
              / (float) (field.matchedPairFieldMatched + field.matchedPairFieldUnmatched);
      }

      private float computeU(final Field field) {
         return (float) (field.unMatchedPairFieldMatched)
              / (float) (field.unMatchedPairFieldMatched + field.unMatchedPairFieldUnmatched);
      }

      @Override
      public String toString() {
         return String.format("f1(%f:%f)",
                              computeM(nationalId), computeU(nationalId));
      }

   }

}
