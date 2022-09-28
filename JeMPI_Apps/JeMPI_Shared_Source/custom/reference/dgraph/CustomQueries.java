DEPRECATED

package org.jembi.jempi.libmpi.dgraph;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jembi.jempi.libmpi.dgraph.Queries.runGoldenRecordQuery;


// https://howtodoinjava.com/design-patterns/creational/singleton-design-pattern-in-java/

final class CustomQueries {

   private CustomQueries() {}

/*
   static RecGoldenRecordList matchGoldenRecordCandidatesByPhoneNumber(final CustomLibMPIDocument document) {
      final String val = document.phoneNumber();
      if (StringUtils.isBlank(val)) {
         return new RecGoldenRecordList(List.of());
      }
      final Map<String, String> vars = Map.of("$phone_number", val);
      return runGoldenRecordQuery(CustomLibMPIQueries.QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_PHONE_NUMBER, vars);
   }
*/

/*
   static RecGoldenRecordList getBlockedPatientsByNationalID(final String val) {
      if (StringUtils.isBlank(val)) {
         return new RecGoldenRecordList(List.of());
      }
      final Map<String, String> vars = Map.of("$national_id", val);
      return runGoldenRecordQuery(CustomLibMPIQueries.QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_NATIONAL_ID, vars);
   }
*/

/*
   static RecGoldenRecordList getDeterministicGoldenRecordCandidates(final CustomLibMPIDocument document) {
      final var givenName = document.givenName();
      final var familyName = document.familyName();
      final var phoneNumber = document.phoneNumber();
      final var nationalID = document.nationalId();
      if (StringUtils.isBlank(nationalID)
          && (StringUtils.isBlank(givenName) || StringUtils.isBlank(familyName) || StringUtils.isBlank(phoneNumber))) {
         return new RecGoldenRecordList(List.of());
      }
      final var vars = Map.of(
            "$national_id", StringUtils.isNotBlank(nationalID) ? nationalID : Queries.EMPTY_FIELD_SENTINEL,
            "$given_name", StringUtils.isNotBlank(givenName) ? givenName : Queries.EMPTY_FIELD_SENTINEL,
            "$family_name", StringUtils.isNotBlank(familyName) ? familyName : Queries.EMPTY_FIELD_SENTINEL,
            "$phone_number", StringUtils.isNotBlank(phoneNumber) ? phoneNumber : Queries.EMPTY_FIELD_SENTINEL);
      final var result = runGoldenRecordQuery(CustomLibMPIQueries.QUERY_DETERMINISTIC_GOLDEN_RECORD_CANDIDATES, vars);
      return new RecGoldenRecordList(result.all()
                                           .stream()
                                           .filter(candidate -> StringUtils.isNotBlank(candidate.nationalId())
                                                                || (StringUtils.isNotBlank(candidate.givenName())
                                                                    && StringUtils.isNotBlank(candidate.familyName())
                                                                    && StringUtils.isNotBlank(candidate.phoneNumber())))
                                           .toList());
   }
*/

/*
   static RecGoldenRecordList matchGoldenRecordsCandidatesByDistance(final CustomLibMPIDocument pdcDocument) {
      final Map<String, String> vars = Map.of(
            "$given_name",
            StringUtils.isNotBlank(pdcDocument.givenName()) ? pdcDocument.givenName() : Queries.EMPTY_FIELD_SENTINEL,
            "$family_name",
            StringUtils.isNotBlank(pdcDocument.familyName()) ? pdcDocument.familyName() : Queries.EMPTY_FIELD_SENTINEL,
            "$city", StringUtils.isNotBlank(pdcDocument.city()) ? pdcDocument.city() : Queries.EMPTY_FIELD_SENTINEL);
      return runGoldenRecordQuery(CustomLibMPIQueries.QUERY_MATCH_GOLDEN_RECORD_CANDIDATES_BY_DISTANCE, vars);
   }
*/

/*
   static List<CustomLibMPIGoldenRecord> getCandidates(final CustomLibMPIDocument doc,
                                                       final boolean applyDeterministicFilter) {

      var candidateGoldenRecords = new LinkedList<CustomLibMPIGoldenRecord>();
      if (applyDeterministicFilter) {
         candidateGoldenRecords = Queries.deterministicFilter(doc);
         if (!candidateGoldenRecords.isEmpty()) {
            return candidateGoldenRecords;
         }
      }

      RecGoldenRecordList block = CustomLibMPIQueries.queryMatchGoldenRecordCandidatesByPhoneNumber(doc.phoneNumber());
      if (block != null) {
         Queries.updateCandidates(candidateGoldenRecords, block);
      }

      block = CustomLibMPIQueries.queryMatchGoldenRecordCandidatesByNationalId(doc.nationalId());
      if (block != null) {
         Queries.updateCandidates(candidateGoldenRecords, block);
      }

      block = CustomLibMPIQueries.queryMatchGoldenRecordCandidatesByDistance(doc);
      if (block != null) {
         Queries.updateCandidates(candidateGoldenRecords, block);
      }
      return candidateGoldenRecords;
   }
*/

}
