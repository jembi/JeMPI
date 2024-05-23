package org.jembi.jempi.shared.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.Config.FIELDS_CONFIG;

/**
 * The type Field tallies.
 */
public record FieldTallies(
      List<FieldTally> fieldTallies) {

   /**
    * The constant FIELD_TALLY_SUM_IDENTITY.
    */
   public static final FieldTallies.FieldTally FIELD_TALLY_SUM_IDENTITY = new FieldTallies.FieldTally(0L, 0L, 0L, 0L);
   /**
    * The constant CUSTOM_FIELD_TALLIES_SUM_IDENTITY.
    */
   public static final FieldTallies CUSTOM_FIELD_TALLIES_SUM_IDENTITY =
         new FieldTallies(FIELDS_CONFIG.demographicFields.stream().map(f -> FIELD_TALLY_SUM_IDENTITY).toList());
   private static final Logger LOGGER = LogManager.getFormatterLogger(FieldTallies.class);
   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
   private static final FieldTally A = new FieldTally(1L, 0L, 0L, 0L);
   private static final FieldTally B = new FieldTally(0L, 1L, 0L, 0L);
   private static final FieldTally C = new FieldTally(0L, 0L, 1L, 0L);
   private static final FieldTally D = new FieldTally(0L, 0L, 0L, 1L);

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
         final FieldTallies.FieldTally fieldTally) {
      LOGGER.debug("%-15s  %,.5f %,.5f",
                   tag,
                   fieldTally.a().doubleValue() / (fieldTally.a().doubleValue() + fieldTally.b().doubleValue()),
                   fieldTally.c().doubleValue() / (fieldTally.c().doubleValue() + fieldTally.d().doubleValue()));
   }

   /**
    * Map field tallies.
    *
    * @param recordsMatch the records match
    * @param left         the left
    * @param right        the right
    * @return the field tallies
    */
   public static FieldTallies map(
         final boolean recordsMatch,
         final DemographicData left,
         final DemographicData right) {
      return new FieldTallies(IntStream
                                    .range(0, FIELDS_CONFIG.demographicFields.size())
                                    .mapToObj(i -> getFieldTally(recordsMatch,
                                                                 left.fields.get(i).value(),
                                                                 right.fields.get(i).value())).toList());

   }

   /**
    * Log field mu.
    */
   public void logFieldMU() {
      LOGGER.debug("Tally derived M&U's");
      IntStream.range(0, FIELDS_CONFIG.demographicFields.size())
               .forEach(i -> logMU(FIELDS_CONFIG.demographicFields.get(i).ccName(), fieldTallies.get(i)));
   }

   /**
    * Sum field tallies.
    *
    * @param r the r
    * @return the field tallies
    */
   public FieldTallies sum(final FieldTallies r) {
      return new FieldTallies(IntStream.range(0, r.fieldTallies.size())
                                       .mapToObj(i -> this.fieldTallies.get(i).sum(r.fieldTallies.get(i)))
                                       .toList());
   }

   /**
    * The type Field tally.
    */
   public record FieldTally(
         Long a,
         Long b,
         Long c,
         Long d) {

      /**
       * Sum field tally.
       *
       * @param r the r
       * @return the field tally
       */
      FieldTally sum(final FieldTally r) {
         return new FieldTally(this.a + r.a,
                               this.b + r.b,
                               this.c + r.c,
                               this.d + r.d);
      }

   }

}
