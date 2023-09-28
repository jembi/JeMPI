package org.jembi.jempi.libmpi.dgraph;

import io.vavr.Function1;
import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jembi.jempi.libmpi.dgraph.DgraphQueries.runGoldenRecordsQuery;

final class CustomDgraphQueries {

   static final List<Function1<CustomDemographicData, DgraphGoldenRecords>> DETERMINISTIC_LINK_FUNCTIONS =
      List.of(CustomDgraphQueries::queryLinkDeterministicA);

   static final List<Function1<CustomDemographicData, DgraphGoldenRecords>> DETERMINISTIC_MATCH_FUNCTIONS =
      List.of(CustomDgraphQueries::queryMatchDeterministicA);

   private static final String QUERY_LINK_DETERMINISTIC_A =
         """
         query query_link_deterministic_a($national_id: string) {
            all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.national_id, $national_id)) {
               uid
               GoldenRecord.source_id {
                  uid
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
            }
         }
         """;

   private static final String QUERY_MATCH_DETERMINISTIC_A =
         """
         query query_match_deterministic_a($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.given_name, $given_name)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.family_name, $family_name)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.phone_number, $phone_number)) {
               C as uid
            }
            all(func:type(GoldenRecord)) @filter(uid(A) AND uid(B) AND uid(C)) {
               uid
               GoldenRecord.source_id {
                  uid
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
            }
         }
         """;

   private static final String QUERY_MATCH_PROBABILISTIC_BLOCK =
         """
         query query_match_probabilistic_block($given_name: string, $family_name: string, $phone_number: string) {
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.given_name, $given_name, 3)) {
               A as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.family_name, $family_name, 3)) {
               B as uid
            }
            var(func:type(GoldenRecord)) @filter(match(GoldenRecord.phone_number, $phone_number, 3)) {
               C as uid
            }
            all(func:type(GoldenRecord)) @filter((uid(A) AND uid(B)) OR (uid(A) AND uid(C)) OR (uid(B) AND uid(C))) {
               uid
               GoldenRecord.source_id {
                  uid
               }
               GoldenRecord.aux_date_created
               GoldenRecord.aux_auto_update_enabled
               GoldenRecord.aux_id
               GoldenRecord.given_name
               GoldenRecord.family_name
               GoldenRecord.gender
               GoldenRecord.dob
               GoldenRecord.city
               GoldenRecord.phone_number
               GoldenRecord.national_id
            }
         }
         """;

   private static DgraphGoldenRecords queryLinkDeterministicA(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.nationalId)) {
         return new DgraphGoldenRecords(List.of());
      }
      final Map<String, String> map = Map.of("$national_id", demographicData.nationalId);
      return runGoldenRecordsQuery(QUERY_LINK_DETERMINISTIC_A, map);
   }

   private static void mergeCandidates(
         final List<CustomDgraphGoldenRecord> goldenRecords,
         final DgraphGoldenRecords block) {
      final var candidates = block.all();
      if (!candidates.isEmpty()) {
         candidates.forEach(candidate -> {
            var found = false;
            for (CustomDgraphGoldenRecord goldenRecord : goldenRecords) {
               if (candidate.goldenId().equals(goldenRecord.goldenId())) {
                  found = true;
                  break;
               }
            }
            if (!found) {
               goldenRecords.add(candidate);
            }
         });
      }
   }

   static List<CustomDgraphGoldenRecord> findLinkCandidates(
      final CustomDemographicData interaction) {
      var result = DgraphQueries.deterministicFilter(DETERMINISTIC_LINK_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      return result;
   }

   private static DgraphGoldenRecords queryMatchDeterministicA(final CustomDemographicData demographicData) {
      final var givenName = demographicData.givenName;
      final var familyName = demographicData.familyName;
      final var phoneNumber = demographicData.phoneNumber;
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var phoneNumberIsBlank = StringUtils.isBlank(phoneNumber);
      if ((givenNameIsBlank || familyNameIsBlank || phoneNumberIsBlank)) {
         return new DgraphGoldenRecords(List.of());
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number",
                             StringUtils.isNotBlank(phoneNumber)
                                   ? phoneNumber
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_MATCH_DETERMINISTIC_A, map);
   }

   private static DgraphGoldenRecords queryMatchProbabilisticBlock(final CustomDemographicData demographicData) {
      final var givenName = demographicData.givenName;
      final var familyName = demographicData.familyName;
      final var phoneNumber = demographicData.phoneNumber;
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var phoneNumberIsBlank = StringUtils.isBlank(phoneNumber);
      if (((givenNameIsBlank || familyNameIsBlank) && (givenNameIsBlank || phoneNumberIsBlank) && (familyNameIsBlank || phoneNumberIsBlank))) {
         return new DgraphGoldenRecords(List.of());
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL,
                             "$phone_number",
                             StringUtils.isNotBlank(phoneNumber)
                                   ? phoneNumber
                                   : DgraphQueries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_MATCH_PROBABILISTIC_BLOCK, map);
   }

   static List<CustomDgraphGoldenRecord> findMatchCandidates(
      final CustomDemographicData interaction) {
      var result = DgraphQueries.deterministicFilter(DETERMINISTIC_MATCH_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      mergeCandidates(result, queryMatchProbabilisticBlock(interaction));
      return result;
   }

   private CustomDgraphQueries() {
   }

}
