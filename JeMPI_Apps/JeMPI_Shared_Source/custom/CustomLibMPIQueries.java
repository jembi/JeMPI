package org.jembi.jempi.libmpi.dgraph;

import org.apache.commons.lang3.StringUtils;
import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jembi.jempi.libmpi.dgraph.Queries.runGoldenRecordsQuery;

class CustomLibMPIQueries {

   static final String QUERY_DETERMINISTIC_GOLDEN_RECORD_CANDIDATES =
         """
         query query_deterministic_golden_record_candidates($given_name: string, $family_name: string, $phone_number: string, $national_id: string) {
            var(func: eq(GoldenRecord.given_name, $given_name)) {
               A as uid
            }
            var(func: eq(GoldenRecord.family_name, $family_name)) {
               B as uid
            }
            var(func: eq(GoldenRecord.phone_number, $phone_number)) {
               C as uid
            }
            var(func: eq(GoldenRecord.national_id, $national_id)) {
               D as uid
            }
            all(func: uid(A,B,C,D)) @filter (uid(D) OR (uid(A) AND uid(B) AND uid(C))) {
               uid
               GoldenRecord.source_id {
                  uid
               }
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
      
   static final String QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_DISTANCE =
         """
         query query_match_golden_record_candidates_by_distance($given_name: string, $family_name: string, $city: string) {
            var(func: match(GoldenRecord.given_name, $given_name, 3)) {
               A as uid
            }
            var(func: match(GoldenRecord.family_name, $family_name, 3)) {
               B as uid
            }
            var(func: match(GoldenRecord.city, $city, 3)) {
               C as uid
            }
            all(func: uid(A,B,C)) @filter ((uid(A) AND uid(B)) OR (uid(A) AND uid(C)) OR (uid(B) AND uid(C))) {
               uid
               GoldenRecord.source_id {
                  uid
               }
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
      
   static final String QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_PHONE_NUMBER =
         """
         query query_match_golden_record_candidates_by_phone_number($phone_number: string) {
            all(func: match(GoldenRecord.phone_number, $phone_number, 3)) {
               uid
               GoldenRecord.source_id {
                  uid
               }
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
      
   static final String QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_NATIONAL_ID =
         """
         query query_match_golden_record_candidates_by_national_id($national_id: string) {
            all(func: match(GoldenRecord.national_id, $national_id, 3)) {
               uid
               GoldenRecord.source_id {
                  uid
               }
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
      

   static LibMPIGoldenRecordList queryDeterministicGoldenRecordCandidates(final CustomDemographicData demographicData) {
      final var givenName = demographicData.givenName();
      final var familyName = demographicData.familyName();
      final var phoneNumber = demographicData.phoneNumber();
      final var nationalId = demographicData.nationalId();
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var phoneNumberIsBlank = StringUtils.isBlank(phoneNumber);
      final var nationalIdIsBlank = StringUtils.isBlank(nationalId);
      if ((nationalIdIsBlank && (givenNameIsBlank || familyNameIsBlank || phoneNumberIsBlank))) {
         return new LibMPIGoldenRecordList(List.of());
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : Queries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : Queries.EMPTY_FIELD_SENTINEL,
                             "$phone_number",
                             StringUtils.isNotBlank(phoneNumber)
                                   ? phoneNumber
                                   : Queries.EMPTY_FIELD_SENTINEL,
                             "$national_id",
                             StringUtils.isNotBlank(nationalId)
                                   ? nationalId
                                   : Queries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_DETERMINISTIC_GOLDEN_RECORD_CANDIDATES, map);
   }

   static LibMPIGoldenRecordList queryMatchGoldenRecordCandidatesByDistance(final CustomDemographicData demographicData) {
      final var givenName = demographicData.givenName();
      final var familyName = demographicData.familyName();
      final var city = demographicData.city();
      final var givenNameIsBlank = StringUtils.isBlank(givenName);
      final var familyNameIsBlank = StringUtils.isBlank(familyName);
      final var cityIsBlank = StringUtils.isBlank(city);
      if (((givenNameIsBlank || familyNameIsBlank) && (givenNameIsBlank || cityIsBlank) && (familyNameIsBlank || cityIsBlank))) {
         return new LibMPIGoldenRecordList(List.of());
      }
      final var map = Map.of("$given_name",
                             StringUtils.isNotBlank(givenName)
                                   ? givenName
                                   : Queries.EMPTY_FIELD_SENTINEL,
                             "$family_name",
                             StringUtils.isNotBlank(familyName)
                                   ? familyName
                                   : Queries.EMPTY_FIELD_SENTINEL,
                             "$city",
                             StringUtils.isNotBlank(city)
                                   ? city
                                   : Queries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordsQuery(QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_DISTANCE, map);
   }

   static LibMPIGoldenRecordList queryMatchGoldenRecordCandidatesByPhoneNumber(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.phoneNumber())) {
         return new LibMPIGoldenRecordList(List.of());
      }
      final Map<String, String> map = Map.of("$phone_number", demographicData.phoneNumber());
      return runGoldenRecordsQuery(QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_PHONE_NUMBER, map);
   }

   static LibMPIGoldenRecordList queryMatchGoldenRecordCandidatesByNationalId(final CustomDemographicData demographicData) {
      if (StringUtils.isBlank(demographicData.nationalId())) {
         return new LibMPIGoldenRecordList(List.of());
      }
      final Map<String, String> map = Map.of("$national_id", demographicData.nationalId());
      return runGoldenRecordsQuery(QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_NATIONAL_ID, map);
   }

   private static void updateCandidates(
         final List<CustomLibMPIGoldenRecord> goldenRecords,
         final LibMPIGoldenRecordList block) {
      final var candidates = block.all();
      if (!candidates.isEmpty()) {
         candidates.forEach(candidate -> {
            var found = false;
            for (CustomLibMPIGoldenRecord goldenRecord : goldenRecords) {
               if (candidate.uid().equals(goldenRecord.uid())) {
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

   static List<CustomLibMPIGoldenRecord> getCandidates(
         final CustomDemographicData patient,
         final boolean applyDeterministicFilter) {

      if (applyDeterministicFilter) {
         final var result = Queries.deterministicFilter(patient);
         if (!result.isEmpty()) {
            return result;
         }
      }
      var result = new LinkedList<CustomLibMPIGoldenRecord>();
      updateCandidates(result, queryMatchGoldenRecordCandidatesByDistance(patient));
      updateCandidates(result, queryMatchGoldenRecordCandidatesByPhoneNumber(patient));
      updateCandidates(result, queryMatchGoldenRecordCandidatesByNationalId(patient));
      return result;
   }

   private CustomLibMPIQueries() {}

}
