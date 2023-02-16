package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.utils.AppUtils;
import org.jembi.jempi.shared.utils.RecordType;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;

import java.lang.reflect.Field;
import java.util.*;

final class Queries {

   static final String EMPTY_FIELD_SENTINEL = "EMPTY_FIELD_SENTINEL";
   private static final Logger LOGGER = LogManager.getLogger(Queries.class);

   private Queries() {
   }

   static LibMPISourceIdList runSourceIdQuery(final String query) {
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, null);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, LibMPISourceIdList.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new LibMPISourceIdList(List.of());
   }

   static LibMPIDGraphPatientRecords runPatientRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIDGraphPatientRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new LibMPIDGraphPatientRecords(List.of());
   }

   static LibMPIGoldenRecords runGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIGoldenRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new LibMPIGoldenRecords(List.of());
   }

   static LibMPIExpandedGoldenRecords runExpandedGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedGoldenRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new LibMPIExpandedGoldenRecords(List.of());
   }

   static PatientRecord getDGraphPatientRecord(final String uid) {
      if (StringUtils.isBlank(uid)) {
         return null;
      }
      final var vars = Map.of("$uid", uid);
      final var patientList = runPatientRecordsQuery(CustomLibMPIConstants.QUERY_GET_PATIENT_BY_UID, vars).all();
      if (AppUtils.isNullOrEmpty(patientList)) {
         return null;
      }
      return patientList.get(0).toPatientRecordWithScore().patientRecord();
   }

   static CustomLibMPIGoldenRecord getGoldenRecordByUid(final String uid) {
      if (StringUtils.isBlank(uid)) {
         return null;
      }
      final var vars = Map.of("$uid", uid);
      final var goldenRecordList = runGoldenRecordsQuery(CustomLibMPIConstants.QUERY_GET_GOLDEN_RECORD_BY_UID, vars)
            .all();

      if (AppUtils.isNullOrEmpty(goldenRecordList)) {
         LOGGER.warn("No goldenRecord for {}", uid);
         return null;
      }
      return goldenRecordList.get(0);
   }

   static List<String> getGoldenUidPatientUidList(final String uid) {
      final String query = String
            .format("""
                    query recordGoldenUidPatientUidList() {
                        list(func: uid(%s)) {
                            uid
                            list: GoldenRecord.patients {
                                uid
                            }
                        }
                    }""", uid);
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, RecUidUidList.class);
         if (response.list().size() == 1) {
            final var list = new ArrayList<String>();
            response.list().get(0).list().forEach(x -> list.add(x.uid()));
            return list;
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   static List<String> getGoldenIdList() {
      final String query = """
                           query recordGoldenId() {
                             list(func: type(GoldenRecord)) {
                               uid
                             }
                           }""";
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIUidList.class);
         final var list = new ArrayList<String>();
         response.list().forEach(x -> list.add(x.uid()));
         return list;
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   private static long getCount(final String query) {
      try {
         final var json = Client.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, LibMPICountList.class);
         return response.list().get(0).count();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return 0L;
   }

   static long countGoldenRecords() {
      final var query = """
                        query recordCount() {
                          list(func: type(GoldenRecord)) {
                            count(uid)
                          }
                        }""";
      return getCount(query);
   }

   static long countGoldenRecordEntities(final String uid) {
      final var query = String.format(
            """
            query recordCount() {
              list(func: uid(%s)) {
                count: count(GoldenRecord.patients)
              }
            }""", uid);
      return getCount(query);
   }

   static long countPatients() {
      final var query = """
                        query recordCount() {
                          list(func: type(PatientRecord)) {
                            count(uid)
                          }
                        }""";
      return getCount(query);
   }

   static LinkedList<CustomLibMPIGoldenRecord> deterministicFilter(final CustomDemographicData patient) {
      final LinkedList<CustomLibMPIGoldenRecord> candidateGoldenRecords = new LinkedList<>();
      var block = CustomLibMPIQueries.queryDeterministicGoldenRecordCandidates(patient);
      if (!block.all().isEmpty()) {
         final List<CustomLibMPIGoldenRecord> list = block.all();
         if (!AppUtils.isNullOrEmpty(list)) {
            candidateGoldenRecords.addAll(list);
         }
      }
      return candidateGoldenRecords;
   }

   static List<CustomLibMPIExpandedGoldenRecord> getExpandedGoldenRecordList(final List<String> idList) {
      final String query = String.format(
            CustomLibMPIConstants.QUERY_GET_EXPANDED_GOLDEN_RECORDS,
            String.join(",", idList));
      final String json = Client.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedGoldenRecords.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   static List<CustomLibMPIExpandedPatientRecord> getExpandedPatientRecords(final List<String> idList) {
      final String query = String.format(CustomLibMPIConstants.QUERY_GET_EXPANDED_PATIENTS,
                                         String.join(",", idList));
      final String json = Client.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedPatientRecords.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }


   private static String camelToSnake(final String str) {
      return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
   }

   private static List<String> getRecordFieldNamesByType(final RecordType recordType) {
      List<String> fieldNames = new ArrayList<String>();
      // Class C = CustomDemographicData.class;
      Field[] fields = CustomDemographicData.class.getDeclaredFields();
      fieldNames.add("uid");
      for (Field field : fields) {
         fieldNames.add(recordType + "." + camelToSnake(field.getName()));
      }
      return fieldNames;
   }

   private static List<String> getSimpleSearchQueryArguments(final List<SimpleSearchRequestPayload.SearchParameter> parameters) {
      List<String> args = new ArrayList<String>();
      for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            args.add(String.format("$%s: string", fieldName));
         }
      }
      return args;
   }

   private static List<String> getCustomSearchQueryArguments(final List<SimpleSearchRequestPayload> payloads) {
      List<String> args = new ArrayList<String>();
      for (int i = 0; i < payloads.size(); i++) {
         List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
         for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
            if (!param.value().isEmpty()) {
               String fieldName = camelToSnake(param.fieldName());
               args.add(String.format("$%s_%d: string", fieldName, i));
            }
         }
      }
      return args;
   }

   private static HashMap<String, String> getSimpleSearchQueryVariables(final List<SimpleSearchRequestPayload.SearchParameter> parameters) {
      final var vars = new HashMap<String, String>();
      for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            String value = param.value();
            vars.put("$" + fieldName, value);
         }
      }
      return vars;
   }

   private static HashMap<String, String> getCustomSearchQueryVariables(final List<SimpleSearchRequestPayload> payloads) {
      final var vars = new HashMap<String, String>();
      for (int i = 0; i < payloads.size(); i++) {
         List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
         for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
            if (!param.value().isEmpty()) {
               String fieldName = camelToSnake(param.fieldName());
               String value = param.value();
               vars.put(String.format("$%s_%d", fieldName, i), value);
            }
         }
      }
      return vars;
   }

   private static String getSimpleSearchQueryFilters(
         final RecordType recordType,
         final List<SimpleSearchRequestPayload.SearchParameter> parameters) {
      List<String> gqlFilters = new ArrayList<String>();
      for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            Integer distance = param.distance();
            if (distance == 0) {
               gqlFilters.add("eq(" + recordType + "." + fieldName + ", $" + fieldName + ")");
            } else {
               gqlFilters.add("match(" + recordType + "." + fieldName + ", $" + fieldName + ", " + distance + ")");
            }
         }
      }
      if (!gqlFilters.isEmpty()) {
         return String.join(" AND ", gqlFilters);
      }
      return "";
   }

   private static String getCustomSearchQueryFilters(
         final RecordType recordType,
         final List<SimpleSearchRequestPayload> payloads) {
      final List<String> gqlOrCondition = new ArrayList<String>();
      for (int i = 0; i < payloads.size(); i++) {
         List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
         List<String> gqlAndCondition = new ArrayList<String>();
         for (SimpleSearchRequestPayload.SearchParameter param : parameters) {
            if (!param.value().isEmpty()) {
               String fieldName = camelToSnake(param.fieldName());
               Integer distance = param.distance();
               if (distance == 0) {
                  gqlAndCondition.add("eq(" + recordType + "." + fieldName + ", $" + fieldName + "_" + i + ")");
               } else {
                  gqlAndCondition.add(
                        "match(" + recordType + "." + fieldName + ", $" + fieldName + "_" + i + ", " + distance + ")");
               }
            }
         }
         if (!gqlAndCondition.isEmpty()) {
            gqlOrCondition.add(String.join(" AND ", gqlAndCondition));
         }
      }
      if (!gqlOrCondition.isEmpty()) {
         return "(" + String.join(") OR (", gqlOrCondition) + ")";
      }
      return "";
   }

   private static String getSearchQueryFunc(
         final RecordType recordType,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      String direction = sortAsc
            ? "asc"
            : "desc";
      String sort = "";
      // Sort by default is by uid
      if (sortBy != null && !sortBy.isBlank() && !sortBy.equals("uid")) {
         sort = String.format(", order%s: %s.%s", direction, recordType, camelToSnake(sortBy));
      }
      return String.format("func: type(%s), first: %d, offset: %d", recordType, limit, offset) + sort;
   }

   private static String getSearchQueryPagination(
         final RecordType recordType,
         final String gqlFilters) {
      return String.format("pagination(func: type(%s)) @filter(%s) {%ntotal: count(uid)%n}", recordType, gqlFilters);
   }

   private static LibMPIExpandedGoldenRecords searchGoldenRecords(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                 ) {
      String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord, offset, limit, sortBy, sortAsc);
      List<String> patientRecordFieldNames = getRecordFieldNamesByType(RecordType.PatientRecord);
      List<String> goldenRecordFieldNames = getRecordFieldNamesByType(RecordType.GoldenRecord);
      String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);

      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += String.join("\n", goldenRecordFieldNames) + "\n";
      gql += RecordType.GoldenRecord + ".patients {\n" + String.join("\n", patientRecordFieldNames) + "}\n";
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      LOGGER.debug("Search Golden Records Query {}", gql);
      LOGGER.debug("Search Golden Records Variables {}", gqlVars);
      return runExpandedGoldenRecordsQuery(gql, gqlVars);
   }

   static LibMPIExpandedGoldenRecords simpleSearchGoldenRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                               ) {
      LOGGER.debug("Simple Search Golden Records Params {}", params);
      String gqlFilters = getSimpleSearchQueryFilters(RecordType.GoldenRecord, params);
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return searchGoldenRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   static LibMPIExpandedGoldenRecords customSearchGoldenRecords(
         final List<SimpleSearchRequestPayload> payloads,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                               ) {
      LOGGER.debug("Custom Search Golden Records Params {}", payloads);
      String gqlFilters = getCustomSearchQueryFilters(RecordType.GoldenRecord, payloads);
      List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
      HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

      return searchGoldenRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   private static LibMPIDGraphPatientRecords searchPatientRecords(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                 ) {
      String gqlFunc = getSearchQueryFunc(RecordType.PatientRecord, offset, limit, sortBy, sortAsc);
      List<String> patientRecordFieldNames = getRecordFieldNamesByType(RecordType.PatientRecord);
      String gqlPagination = getSearchQueryPagination(RecordType.PatientRecord, gqlFilters);

      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += String.join("\n", patientRecordFieldNames) + "\n";
      gql += RecordType.PatientRecord + ".golden_record_list {\nuid\n}\n";
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      LOGGER.debug("Simple Search Patient Records Query {}", gql);
      LOGGER.debug("Simple Search Patient Records Variables {}", gqlVars);
      return runPatientRecordsQuery(gql, gqlVars);
   }

   static LibMPIDGraphPatientRecords simpleSearchPatientRecords(
         final List<SimpleSearchRequestPayload.SearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                               ) {
      LOGGER.debug("Simple Search Patient Records Params {}", params);
      String gqlFilters = getSimpleSearchQueryFilters(RecordType.PatientRecord, params);
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return searchPatientRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   static LibMPIDGraphPatientRecords customSearchPatientRecords(
         final List<SimpleSearchRequestPayload> payloads,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                               ) {
      LOGGER.debug("Simple Search Patient Records Params {}", payloads);
      String gqlFilters = getCustomSearchQueryFilters(RecordType.PatientRecord, payloads);
      List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
      HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

      return searchPatientRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

}
