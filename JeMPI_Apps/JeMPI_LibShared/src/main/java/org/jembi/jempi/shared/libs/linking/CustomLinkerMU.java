package org.jembi.jempi.shared.libs.linking;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
      updateMatchedPair(fields.givenName, patient.givenName, goldenRecord.givenName);
      updateMatchedPair(fields.familyName, patient.familyName, goldenRecord.familyName);
      updateMatchedPair(fields.gender, patient.gender, goldenRecord.gender);
      updateMatchedPair(fields.dob, patient.dob, goldenRecord.dob);
      updateMatchedPair(fields.city, patient.city, goldenRecord.city);
      updateMatchedPair(fields.phoneNumber, patient.phoneNumber, goldenRecord.phoneNumber);
      updateMatchedPair(fields.nationalId, patient.nationalId, goldenRecord.nationalId);
      LOGGER.debug("{}", fields);
   }

   void updateMissmatchSums(
         final CustomDemographicData patient,
         final CustomDemographicData goldenRecord) {
      updateUnMatchedPair(fields.givenName, patient.givenName, goldenRecord.givenName);
      updateUnMatchedPair(fields.familyName, patient.familyName, goldenRecord.familyName);
      updateUnMatchedPair(fields.gender, patient.gender, goldenRecord.gender);
      updateUnMatchedPair(fields.dob, patient.dob, goldenRecord.dob);
      updateUnMatchedPair(fields.city, patient.city, goldenRecord.city);
      updateUnMatchedPair(fields.phoneNumber, patient.phoneNumber, goldenRecord.phoneNumber);
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
      final Field givenName = new Field(JARO_WINKLER_SIMILARITY, 0.92);
      final Field familyName = new Field(JARO_WINKLER_SIMILARITY, 0.92);
      final Field gender = new Field(JARO_WINKLER_SIMILARITY, 0.92);
      final Field dob = new Field(JARO_WINKLER_SIMILARITY, 0.92);
      final Field city = new Field(JARO_WINKLER_SIMILARITY, 0.92);
      final Field phoneNumber = new Field(JARO_WINKLER_SIMILARITY, 0.92);
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
         return String.format(Locale.ROOT, "f1(%f:%f) f2(%f:%f) f3(%f:%f) f4(%f:%f) f5(%f:%f) f6(%f:%f) f7(%f:%f)",
                              computeM(givenName), computeU(givenName),
                              computeM(familyName), computeU(familyName),
                              computeM(gender), computeU(gender),
                              computeM(dob), computeU(dob),
                              computeM(city), computeU(city),
                              computeM(phoneNumber), computeU(phoneNumber),
                              computeM(nationalId), computeU(nationalId));
      }

   }
           
   public static final class FieldMatchInfo {
      LinkerProbabilistic.FieldScoreInfo givenName = null;
      LinkerProbabilistic.FieldScoreInfo familyName = null;
      LinkerProbabilistic.FieldScoreInfo gender = null;
      LinkerProbabilistic.FieldScoreInfo dob = null;
      LinkerProbabilistic.FieldScoreInfo city = null;
      LinkerProbabilistic.FieldScoreInfo phoneNumber = null;
      LinkerProbabilistic.FieldScoreInfo nationalId = null;
      public FieldMatchInfo(final CustomDemographicData patient,
                                                    final CustomDemographicData goldenRecord) {
         this.givenName = LinkerProbabilistic.fieldScoreInfo(patient.givenName, goldenRecord.givenName, LINKER_FIELDS.get("givenName"));
         this.familyName = LinkerProbabilistic.fieldScoreInfo(patient.familyName, goldenRecord.familyName, LINKER_FIELDS.get("familyName"));
         this.gender = LinkerProbabilistic.fieldScoreInfo(patient.gender, goldenRecord.gender, LINKER_FIELDS.get("gender"));
         this.dob = LinkerProbabilistic.fieldScoreInfo(patient.dob, goldenRecord.dob, LINKER_FIELDS.get("dob"));
         this.city = LinkerProbabilistic.fieldScoreInfo(patient.city, goldenRecord.city, LINKER_FIELDS.get("city"));
         this.phoneNumber = LinkerProbabilistic.fieldScoreInfo(patient.phoneNumber, goldenRecord.phoneNumber, LINKER_FIELDS.get("phoneNumber"));
         this.nationalId = LinkerProbabilistic.fieldScoreInfo(patient.nationalId, goldenRecord.nationalId, LINKER_FIELDS.get("nationalId"));
      }

      public Map<String, LinkerProbabilistic.FieldScoreInfo> toMap() {
          return Map.ofEntries(
            Map.entry("givenName", this.givenName),
            Map.entry("familyName", this.familyName),
            Map.entry("gender", this.gender),
            Map.entry("dob", this.dob),
            Map.entry("city", this.city),
            Map.entry("phoneNumber", this.phoneNumber),
            Map.entry("nationalId", this.nationalId)
         );
       } 
    }
   public static final Map<String, LinkerProbabilistic.Field> LINKER_FIELDS = Map.ofEntries(
      Map.entry("givenName",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8806329F, 0.0026558F)),
      Map.entry("familyName",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9140443F, 6.275E-4F)),
      Map.entry("gender",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.9468393F, 0.4436446F)),
      Map.entry("dob",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.7856196F, 4.65E-5F)),
      Map.entry("city",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8445694F, 0.0355741F)),
      Map.entry("phoneNumber",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.84085F, 4.0E-7F)),
      Map.entry("nationalId",  new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, List.of(0.92F), 0.8441029F, 2.0E-7F))
   );
}
