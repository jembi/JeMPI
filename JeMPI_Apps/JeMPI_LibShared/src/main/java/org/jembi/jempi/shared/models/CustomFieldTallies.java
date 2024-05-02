package org.jembi.jempi.shared.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.jembi.jempi.shared.models.CustomDemographicData.*;

public record CustomFieldTallies(
      FieldTally givenName,
      FieldTally familyName,
      FieldTally gender,
      FieldTally dob,
      FieldTally city,
      FieldTally phoneNumber,
      FieldTally nationalId) {

   private static final Logger LOGGER = LogManager.getFormatterLogger(CustomFieldTallies.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
   private static final FieldTally A = new FieldTally(1L, 0L, 0L, 0L);
   private static final FieldTally B = new FieldTally(0L, 1L, 0L, 0L);
   private static final FieldTally C = new FieldTally(0L, 0L, 1L, 0L);
   private static final FieldTally D = new FieldTally(0L, 0L, 0L, 1L);
   public static final CustomFieldTallies.FieldTally FIELD_TALLY_SUM_IDENTITY = new CustomFieldTallies.FieldTally(0L, 0L, 0L, 0L);
   public static final CustomFieldTallies CUSTOM_FIELD_TALLIES_SUM_IDENTITY = new CustomFieldTallies(
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY,
      FIELD_TALLY_SUM_IDENTITY);

   private static FieldTally getFieldTally(
      final boolean recordsMatch,
      final String left,
      final String right) {
      if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
         return FIELD_TALLY_SUM_IDENTITY;
      }
      final var fieldMatches = JARO_WINKLER_SIMILARITY.apply(left.toLowerCase(), right.toLowerCase()) >= 0.97;
      if (recordsMatch) {
         if (fieldMatches) {
            return A;
         } else {
            return B;
         }
      } else {
         if (fieldMatches) {
            return C;
         } else {
            return D;
         }
      }
   }

   private static void logMU(
         final String tag,
         final CustomFieldTallies.FieldTally fieldTally) {
      LOGGER.debug("%-15s  %,.5f %,.5f",
                   tag,
                   fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
                   fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
   }

   public static CustomFieldTallies map(
         final boolean recordsMatch,
         final DemographicData left,
         final DemographicData right) {
      return new CustomFieldTallies(getFieldTally(recordsMatch, left.fields.get(0).value(),
                                                                right.fields.get(0).value()),
                                    getFieldTally(recordsMatch, left.fields.get(1).value(),
                                                                right.fields.get(1).value()),
                                    getFieldTally(recordsMatch, left.fields.get(2).value(),
                                                                right.fields.get(2).value()),
                                    getFieldTally(recordsMatch, left.fields.get(3).value(),
                                                                right.fields.get(3).value()),
                                    getFieldTally(recordsMatch, left.fields.get(4).value(),
                                                                right.fields.get(4).value()),
                                    getFieldTally(recordsMatch, left.fields.get(5).value(),
                                                                right.fields.get(5).value()),
                                    getFieldTally(recordsMatch, left.fields.get(6).value(),
                                                                right.fields.get(6).value()));
   }

   public void logFieldMU() {
      LOGGER.debug("Tally derived M&U's");
      logMU("givenName", givenName);
      logMU("familyName", familyName);
      logMU("gender", gender);
      logMU("dob", dob);
      logMU("city", city);
      logMU("phoneNumber", phoneNumber);
      logMU("nationalId", nationalId);
   }

   public CustomFieldTallies sum(final CustomFieldTallies r) {
      return new CustomFieldTallies(this.givenName.sum(r.givenName),
                                    this.familyName.sum(r.familyName),
                                    this.gender.sum(r.gender),
                                    this.dob.sum(r.dob),
                                    this.city.sum(r.city),
                                    this.phoneNumber.sum(r.phoneNumber),
                                    this.nationalId.sum(r.nationalId));
   }

   public record FieldTally(
         Long a,
         Long b,
         Long c,
         Long d) {

      FieldTally sum(final FieldTally r) {
         return new FieldTally(this.a + r.a,
                               this.b + r.b,
                               this.c + r.c,
                               this.d + r.d);
      }

   }

}
