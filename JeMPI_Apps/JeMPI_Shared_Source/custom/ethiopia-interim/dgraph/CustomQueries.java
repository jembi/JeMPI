DEPRECATED

package org.jembi.jempi.libmpi.dgraph;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jembi.jempi.libmpi.dgraph.CustomLibMPIConstants.*;
import static org.jembi.jempi.libmpi.dgraph.Queries.runGoldenRecordQuery;


// https://howtodoinjava.com/design-patterns/creational/singleton-design-pattern-in-java/

final class CustomQueries {

/*
   private static final String QUERY_DETERMINISTIC_CANDIDATES = String.format(
         """                        
         query blockedPatients($givenName:string, $fathersName:string, $phoneNumber:string) {
             var(func: eq(%s, $givenName)) {
                 B as uid
             }
             var(func: eq(%s, $fathersName)) {
                 C as uid
             }
             var(func: eq(%s, $phoneNumber)) {
                 D as uid
             }
             all(func: uid(B,C,D)) @filter(uid(B) AND uid(C) AND uid(D)) {
                 uid
                 expand(GoldenRecord)
            }
         }""", PREDICATE_GOLDEN_RECORD_GIVEN_NAME,
         PREDICATE_GOLDEN_RECORD_FATHERS_NAME,
         PREDICATE_GOLDEN_RECORD_PHONE_NUMBER);
*/

/*
   private static final String QUERY_MATCH_GOLDEN_RECORD_BY_PHONE_NUMBER = String.format(
         """
         query matchGoldenRecordByPhoneNumber($phoneNumber: string) {
            all(func: match(%sr, $phoneNumber, 3)) {
                uid
                expand(GoldenRecord)
            }
         }
         """,
         PREDICATE_GOLDEN_RECORD_PHONE_NUMBER);
*/

/*
   private static final String QUERY_GET_GOLDEN_RECORDS_BY_DISTANCE = String.format(
         """                        
         query blockedPatients($givenName: string, $fathersName: string, $city: string) {
             var(func: match(%s, $givenName, 3)) {
                 B as uid
             }
             var(func: match(%s, $fathersName, 3) ) {
                 A as uid
             }
             var(func: match(%s, $city, 3))  {
                 C as uid
             }
             all(func: uid(A,B,C)) @filter((uid(A) AND uid(B)) OR (uid(A) AND uid(C)) OR (uid(B) AND uid(C))) {
                 uid
                 expand(GoldenRecord)
            }
         }""", PREDICATE_GOLDEN_RECORD_GIVEN_NAME,
         PREDICATE_GOLDEN_RECORD_FATHERS_NAME,
         PREDICATE_GOLDEN_RECORD_CITY);
*/

   private CustomQueries() {}

   static RecGoldenRecordList getBlockedPatientsByPhoneNumber(final CustomLibMPIDocument document) {
      final String phoneNumber = document.phoneNumber();
      if (StringUtils.isBlank(phoneNumber)) {
         return new RecGoldenRecordList(List.of());
      }
      final Map<String, String> vars = Map.of("$phone_number", phoneNumber);
      return runGoldenRecordQuery(CustomLibMPIQueries.QUERY_MATCH_GOLDEN_RECORDS_BY_PHONE_NUMBER, vars);
   }

   static RecGoldenRecordList getDeterministicGoldenRecord(final CustomLibMPIDocument document) {
      final var givenName = document.nameGiven();
      final var fathersName = document.nameFather();
      final var phoneNumber = document.phoneNumber();
      if ((StringUtils.isBlank(givenName) || StringUtils.isBlank(fathersName) || StringUtils.isBlank(phoneNumber))) {
         return new RecGoldenRecordList(List.of());
      }
      final Map<String, String> vars = Map.of(
            "$name_given", StringUtils.isNotEmpty(givenName) ? givenName : StringUtils.EMPTY,
            "$name_father", StringUtils.isNotEmpty(fathersName) ? fathersName : StringUtils.EMPTY,
            "$phone_number", StringUtils.isNotEmpty(phoneNumber) ? phoneNumber : StringUtils.EMPTY);
      final var result = runGoldenRecordQuery(CustomLibMPIQueries.QUERY_DETERMINISTIC_CANDIDATES, vars);
      return new RecGoldenRecordList(result.all()
                                           .stream()
                                           .filter(candidate -> StringUtils.isNotBlank(candidate.nameGiven()) &&
                                                                StringUtils.isNotBlank(candidate.nameFather()) &&
                                                                StringUtils.isNotBlank(candidate.phoneNumber()))
                                           .toList());
   }

   static RecGoldenRecordList getBlockedPatientsByDistance(final CustomLibMPIDocument pdcDocument) {
      final Map<String, String> vars = Map.of(
            "$name_given", StringUtils.isNotEmpty(pdcDocument.nameGiven()) ? pdcDocument.nameGiven() : StringUtils.EMPTY,
            "$name_father", StringUtils.isNotEmpty(pdcDocument.nameFather()) ? pdcDocument.nameFather() : StringUtils.EMPTY,
            "$city", StringUtils.isNotEmpty(pdcDocument.city()) ? pdcDocument.city() : StringUtils.EMPTY);
      return runGoldenRecordQuery(CustomLibMPIQueries.QUERY_MATCH_GOLDEN_RECORDS_BY_DISTANCE, vars);
   }

   static List<CustomLibMPIGoldenRecord> getCandidates(final CustomLibMPIDocument doc, final boolean applyDeterministicFilter) {

      var candidateGoldenRecords = new LinkedList<CustomLibMPIGoldenRecord>();
      if (applyDeterministicFilter) {
         candidateGoldenRecords = Queries.deterministicFilter(doc);
         if (!candidateGoldenRecords.isEmpty()) {
            return candidateGoldenRecords;
         }
      }

      RecGoldenRecordList block = getBlockedPatientsByPhoneNumber(doc);
      if (block != null) {
         Queries.updateCandidates(candidateGoldenRecords, block);
      }

      block = getBlockedPatientsByDistance(doc);
      if (block != null) {
         Queries.updateCandidates(candidateGoldenRecords, block);
      }
      return candidateGoldenRecords;
   }

}
