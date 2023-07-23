package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.Function1;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.LocalDate;
import java.util.*;

final class DgraphQueries {

   static final String EMPTY_FIELD_SENTINEL = "EMPTY_FIELD_SENTINEL";
   private static final Logger LOGGER = LogManager.getLogger(DgraphQueries.class);

   private DgraphQueries() {
   }

   static DgraphSourceIds runSourceIdQuery(final String query) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, DgraphSourceIds.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new DgraphSourceIds(List.of());
   }

   static DgraphInteractions runInteractionsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, DgraphInteractions.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new DgraphInteractions(List.of());
   }

   static DgraphPaginatedUidList runfilterGidsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, DgraphPaginatedUidList.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphPaginatedUidList(List.of());
   }

   static DgraphGoldenRecords runGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, DgraphGoldenRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphGoldenRecords(List.of());
   }

   static DgraphExpandedGoldenRecords runExpandedGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return AppUtils.OBJECT_MAPPER.readValue(json, DgraphExpandedGoldenRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphExpandedGoldenRecords(List.of());
   }

   static Interaction findInteraction(final String interactionId) {
      if (StringUtils.isBlank(interactionId)) {
         return null;
      }
      final var vars = Map.of("$uid", interactionId);
      final var interactionList = runInteractionsQuery(CustomDgraphConstants.QUERY_GET_INTERACTION_BY_UID, vars).all();
      if (AppUtils.isNullOrEmpty(interactionList)) {
         return null;
      }
      return interactionList.get(0).toInteractionWithScore().interaction();
   }

   static CustomDgraphGoldenRecord findDgraphGoldenRecord(final String goldenId) {
      if (StringUtils.isBlank(goldenId)) {
         return null;
      }
      final var vars = Map.of("$uid", goldenId);
      final var goldenRecordList = runGoldenRecordsQuery(CustomDgraphConstants.QUERY_GET_GOLDEN_RECORD_BY_UID, vars)
            .all();

      if (AppUtils.isNullOrEmpty(goldenRecordList)) {
         LOGGER.warn("No goldenRecord for {}", goldenId);
         return null;
      }
      return goldenRecordList.get(0);
   }

   static List<String> findExpandedGoldenIds(final String goldenId) {
      final String query = String
            .format("""
                    query recordGoldenUidInteractionUidList() {
                        list(func: uid(%s)) {
                            uid
                            list: GoldenRecord.interactions {
                                uid
                            }
                        }
                    }""", goldenId);
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, DgraphUidUidList.class);
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

   static List<String> getGoldenIds() {
      final String query = """
                           query recordGoldenId() {
                             list(func: type(GoldenRecord)) {
                               uid
                             }
                           }""";
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, DgraphUidList.class);
         final var list = new ArrayList<String>();
         response.list().forEach(x -> list.add(x.uid()));
         return list;
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   static List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      final String query = String.format(
            """
            query recordGoldenIds() {
              list(func: type(GoldenRecord), offset: %d, first: %d) {
                uid
              }
            }""", offset, length);
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, DgraphUidList.class);
         return response.list()
                        .stream()
                        .map(DgraphUid::uid)
                        .sorted((o1, o2) -> Long.compare(Long.parseLong(o2.substring(2), 16),
                                                         Long.parseLong(o1.substring(2), 16)))
                        .toList();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   private static long getCount(final String query) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = AppUtils.OBJECT_MAPPER.readValue(json, DgraphCountList.class);
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

   static long countGoldenRecordEntities(final String goldenId) {
      final var query = String.format(
            """
            query recordCount() {
              list(func: uid(%s)) {
                count: count(GoldenRecord.interactions)
              }
            }""", goldenId);
      return getCount(query);
   }

   static long countInteractions() {
      final var query = """
                        query recordCount() {
                          list(func: type(Interaction)) {
                            count(uid)
                          }
                        }""";
      return getCount(query);
   }

   static LinkedList<CustomDgraphGoldenRecord> deterministicFilter(final CustomDemographicData interaction) {
      final LinkedList<CustomDgraphGoldenRecord> candidateGoldenRecords = new LinkedList<>();
      for (Function1<CustomDemographicData, DgraphGoldenRecords> deterministicFunction : CustomDgraphQueries.DETERMINISTIC_FUNCTIONS) {
         final var block = deterministicFunction.apply(interaction);
         if (!block.all().isEmpty()) {
            final var list = block.all();
            if (!AppUtils.isNullOrEmpty(list)) {
               candidateGoldenRecords.addAll(list);
               return candidateGoldenRecords;
            }
         }
      }
      return candidateGoldenRecords;
   }

   static List<CustomDgraphExpandedInteraction> findExpandedInteractions(final List<String> ids) {
      final String query = String.format(CustomDgraphConstants.QUERY_GET_EXPANDED_INTERACTIONS,
                                         String.join(",", ids));
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, DgraphExpandedInteractions.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   static List<CustomDgraphGoldenRecord> findGoldenRecords(final List<String> ids) {
      final String query = String.format(
            CustomDgraphConstants.QUERY_GET_GOLDEN_RECORDS,
            String.join(",", ids));
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, DgraphGoldenRecords.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   static List<CustomDgraphExpandedGoldenRecord> getExpandedGoldenRecords(final List<String> ids) {
      final String query = String.format(
            CustomDgraphConstants.QUERY_GET_EXPANDED_GOLDEN_RECORDS,
            String.join(",", ids));
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, DgraphExpandedGoldenRecords.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   private static String camelToSnake(final String str) {
      return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
   }

/*
   private static List<String> findRecordFieldNamesByType(final RecordType recordType) {
      List<String> fieldNames = new ArrayList<>();
      // Class C = CustomDemographicData.class
      Field[] fields = CustomDemographicData.class.getDeclaredFields();
      fieldNames.add("uid");
      for (Field field : fields) {
         fieldNames.add(recordType + "." + camelToSnake(field.getName()));
      }
      return fieldNames;
   }
*/

   private static List<String> getSimpleSearchQueryArguments(final List<ApiModels.ApiSearchParameter> parameters) {
      List<String> args = new ArrayList<>();
      for (ApiModels.ApiSearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            args.add(String.format("$%s: string", fieldName));
         }
      }
      return args;
   }

   private static List<String> getCustomSearchQueryArguments(final List<ApiModels.ApiSimpleSearchRequestPayload> payloads) {
      List<String> args = new ArrayList<>();
      for (int i = 0; i < payloads.size(); i++) {
         List<ApiModels.ApiSearchParameter> parameters = payloads.get(i).parameters();
         for (ApiModels.ApiSearchParameter param : parameters) {
            if (!param.value().isEmpty()) {
               String fieldName = camelToSnake(param.fieldName());
               args.add(String.format("$%s_%d: string", fieldName, i));
            }
         }
      }
      return args;
   }

   private static HashMap<String, String> getSimpleSearchQueryVariables(final List<ApiModels.ApiSearchParameter> parameters) {
      final var vars = new HashMap<String, String>();
      for (ApiModels.ApiSearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            String value = param.value();
            vars.put("$" + fieldName, value);
         }
      }
      return vars;
   }

   private static HashMap<String, String> getCustomSearchQueryVariables(final List<ApiModels.ApiSimpleSearchRequestPayload> payloads) {
      final var vars = new HashMap<String, String>();
      for (int i = 0; i < payloads.size(); i++) {
         final var parameters = payloads.get(i).parameters();
         for (var param : parameters) {
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
         final List<ApiModels.ApiSearchParameter> parameters) {
      List<String> gqlFilters = new ArrayList<>();
      for (ApiModels.ApiSearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = camelToSnake(param.fieldName());
            Integer distance = param.distance();
            String value = param.value();
            if (distance == 0) {
               gqlFilters.add("regexp(" + recordType + "." + fieldName + ", /^" + value + "$/i)");
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
         final List<ApiModels.ApiSimpleSearchRequestPayload> payloads) {
      final List<String> gqlOrCondition = new ArrayList<>();
      for (int i = 0; i < payloads.size(); i++) {
         List<ApiModels.ApiSearchParameter> parameters = payloads.get(i).parameters();
         List<String> gqlAndCondition = new ArrayList<>();
         for (ApiModels.ApiSearchParameter param : parameters) {
            if (!param.value().isEmpty()) {
               String fieldName = camelToSnake(param.fieldName());
               Integer distance = param.distance();
               String value = param.value();
               if (distance == 0) {
                  gqlAndCondition.add("regexp(" + recordType + "." + fieldName + ", /^" + value + "$/i)");
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

   private static DgraphExpandedGoldenRecords searchGoldenRecords(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                                 ) {
      String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord, offset, limit, sortBy, sortAsc);
      String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);

      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += CustomDgraphConstants.EXPANDED_GOLDEN_RECORD_FIELD_NAMES;
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      LOGGER.debug("Search Golden Records Query {}", gql);
      LOGGER.debug("Search Golden Records Variables {}", gqlVars);
      return runExpandedGoldenRecordsQuery(gql, gqlVars);
   }

   static DgraphExpandedGoldenRecords simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
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

   static DgraphExpandedGoldenRecords customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> payloads,
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

   private static DgraphInteractions searchInteractions(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                       ) {
      String gqlFunc = getSearchQueryFunc(RecordType.Interaction, offset, limit, sortBy, sortAsc);
      String gqlPagination = getSearchQueryPagination(RecordType.Interaction, gqlFilters);
      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += CustomDgraphConstants.INTERACTION_FIELD_NAMES;
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      LOGGER.debug("Simple Search Interactions Query {}", gql);
      LOGGER.debug("Simple Search Interactions Variables {}", gqlVars);
      return runInteractionsQuery(gql, gqlVars);
   }

   private static DgraphPaginatedUidList filterGidsFunc(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                       ) {
      String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord, offset, limit, sortBy, sortAsc);
      String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);
      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += "uid";
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      LOGGER.debug("Filter Gids Query {}", gql);
      LOGGER.debug("Filter Gids Variables {}", gqlVars);
      return runfilterGidsQuery(gql, gqlVars);
   }

   static DgraphPaginatedUidList filterGidsWithParams(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDate createdAt,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                     ) {
      LOGGER.debug("Filter Gids Params {}", params);
      // String dateFilter = String.format("lt(GoldenRecord.aux_date_created, %s)", createdAt);
      String filter = getSimpleSearchQueryFilters(RecordType.GoldenRecord, params);
      // String gqlFilters = filter.length() > 0 ? filter + " AND " + dateFilter : dateFilter;
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return filterGidsFunc(filter, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   static DgraphInteractions simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                     ) {
      LOGGER.debug("Simple Search Interactions Params {}", params);
      String gqlFilters = getSimpleSearchQueryFilters(RecordType.Interaction, params);
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return searchInteractions(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   static DgraphInteractions customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> payloads,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc
                                                     ) {
      LOGGER.debug("Simple Search Interactions Params {}", payloads);
      String gqlFilters = getCustomSearchQueryFilters(RecordType.Interaction, payloads);
      List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
      HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

      return searchInteractions(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

}
