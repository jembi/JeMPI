package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.Config.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

/**
 * The type Dgraph queries.
 */
final class DgraphQueries {

   /**
    * The Empty field sentinel.
    */
   static final String EMPTY_FIELD_SENTINEL = "EMPTY_FIELD_SENTINEL";
   private static final Logger LOGGER = LogManager.getLogger(DgraphQueries.class);
   private static final String GET_SOURCE_ID =
         """
         query query_source_id($facility_id: string, $patient_id: string) {
           var(func: eq(SourceId.facility, $facility_id)) {
             A as uid
           }
           var(func: eq(SourceId.patient, $patient_id)) {
             B as uid
           }
           all(func: uid(A,B)) @filter (uid(A) AND uid(B)) {
             uid
             expand(SourceId)
           }
         }
         """;
   private static final String GET_EXPANDED_SOURCE_ID_LIST =
         """
         query query_expanded_source_id($facility_id: string, $patient_id: string) {
           var(func: eq(SourceId.facility, $facility_id)) {
             A as uid
           }
           var(func: eq(SourceId.patient, $patient_id)) {
             B as uid
           }
           all(func: uid(A,B)) @filter (uid(A) AND uid(B)) {
             uid
             expand(SourceId)
             ~GoldenRecord.source_id {
               uid
               expand(GoldenRecord)
             }
           }
         }
         """;
   private static final String RECORD_GOLDEN_UID_INTERACTION_UID_LIST =
         """
         query recordGoldenUidInteractionUidList($gid: string) {
           list(func: uid($gid)) {
             uid
             list: GoldenRecord.interactions {
               uid
             }
           }
         }""";

   private static final String[] DETERMINISTIC_LINK_QUERIES;
   private static final List<PartialFunction<DemographicData, List<GoldenRecord>>> DETERMINISTIC_LINK_FUNCTIONS;

   private static final String[] PROBABILISTIC_LINK_BLOCK_QUERIES;
   private static final List<PartialFunction<DemographicData, List<GoldenRecord>>> PROBABILISTIC_LINK_BLOCK_FUNCTIONS;

   private static final String[] DETERMINISTIC_MATCH_QUERIES;
   private static final List<PartialFunction<DemographicData, List<GoldenRecord>>> DETERMINISTIC_MATCH_FUNCTIONS;

   private static final String[] PROBABILISTIC_MATCH_BLOCK_QUERIES;
   private static final List<PartialFunction<DemographicData, List<GoldenRecord>>> PROBABILISTIC_MATCH_BLOCK_FUNCTIONS;

   static {
      DETERMINISTIC_LINK_QUERIES = new String[LINKER_CONFIG.deterministicLinkPrograms.size()];
      DETERMINISTIC_LINK_FUNCTIONS = new ArrayList<>();
      if (JSON_CONFIG.rules().link() == null || JSON_CONFIG.rules().link().probabilistic() == null) {
         PROBABILISTIC_LINK_BLOCK_QUERIES = new String[1];
      } else {
         PROBABILISTIC_LINK_BLOCK_QUERIES = new String[JSON_CONFIG.rules().link().probabilistic().size()];
      }
      PROBABILISTIC_LINK_BLOCK_FUNCTIONS = new ArrayList<>();


      DETERMINISTIC_MATCH_QUERIES = new String[LINKER_CONFIG.deterministicLinkPrograms.size()];
      DETERMINISTIC_MATCH_FUNCTIONS = new ArrayList<>();
      if (JSON_CONFIG.rules().matchNotification() == null || JSON_CONFIG.rules().matchNotification().probabilistic() == null) {
         PROBABILISTIC_MATCH_BLOCK_QUERIES = new String[1];
      } else {
         PROBABILISTIC_MATCH_BLOCK_QUERIES = new String[JSON_CONFIG.rules().matchNotification().probabilistic().size()];
      }
      PROBABILISTIC_MATCH_BLOCK_FUNCTIONS = new ArrayList<>();


      /* REFERENCE  LINK */
      DETERMINISTIC_LINK_QUERIES[0] = CustomDgraphQueries.QUERY_LINK_DETERMINISTIC_A;
      DETERMINISTIC_LINK_QUERIES[1] = CustomDgraphQueries.QUERY_LINK_DETERMINISTIC_B;
      IntStream.range(0, LINKER_CONFIG.deterministicLinkPrograms.size())
               .forEach(i -> DETERMINISTIC_LINK_FUNCTIONS.add(i, demographicData -> queryLinkDeterministic(demographicData,
                                                                                                           i,
                                                                                                           DETERMINISTIC_LINK_QUERIES)));
      PROBABILISTIC_LINK_BLOCK_QUERIES[0] = CustomDgraphQueries.QUERY_LINK_PROBABILISTIC_BLOCK;
      PROBABILISTIC_LINK_BLOCK_FUNCTIONS.addFirst(demographicData -> queryLinkProbabilisticBlock(demographicData,
                                                                                                 0,
                                                                                                 PROBABILISTIC_LINK_BLOCK_QUERIES));

      DETERMINISTIC_MATCH_QUERIES[0] = "";
      IntStream.range(0, LINKER_CONFIG.deterministicMatchPrograms.size())
               .forEach(i -> DETERMINISTIC_MATCH_FUNCTIONS.add(i, demographicData -> queryLinkDeterministic(demographicData,
                                                                                                            i,
                                                                                                            DETERMINISTIC_MATCH_QUERIES)));
      PROBABILISTIC_MATCH_BLOCK_QUERIES[0] = null;
      PROBABILISTIC_MATCH_BLOCK_FUNCTIONS.addFirst(demographicData -> queryMatchProbabilisticBlock(demographicData,
                                                                                                   0,
                                                                                                   PROBABILISTIC_MATCH_BLOCK_QUERIES));


      /* REFERENCE LINK_VALIDATE_MATCH */
//      DETERMINISTIC_LINK_QUERIES[0] = CustomDgraphQueries.QUERY_LINK_DETERMINISTIC_A;
//      IntStream.range(0, LINKER_CONFIG.deterministicLinkPrograms.size())
//               .forEach(i -> DETERMINISTIC_LINK_FUNCTIONS.add(i, demographicData -> queryLinkDeterministic(demographicData,
//                                                                                                           i,
//                                                                                                           DETERMINISTIC_LINK_QUERIES)));
//      PROBABILISTIC_LINK_BLOCK_QUERIES[0] = null;
//      PROBABILISTIC_LINK_BLOCK_FUNCTIONS.addFirst(demographicData -> queryLinkProbabilisticBlock(demographicData,
//                                                                                                 0,
//                                                                                                 PROBABILISTIC_LINK_BLOCK_QUERIES));
//
//      DETERMINISTIC_MATCH_QUERIES[0] = CustomDgraphQueries.QUERY_MATCH_DETERMINISTIC_A;
//      IntStream.range(0, LINKER_CONFIG.deterministicMatchPrograms.size())
//               .forEach(i -> DETERMINISTIC_MATCH_FUNCTIONS.add(i, demographicData -> queryMatchDeterministic(demographicData,
//                                                                                                             i,
//                                                                                                             DETERMINISTIC_MATCH_QUERIES)));
//      PROBABILISTIC_MATCH_BLOCK_QUERIES[0] = CustomDgraphQueries.QUERY_MATCH_PROBABILISTIC_BLOCK;
//      PROBABILISTIC_MATCH_BLOCK_FUNCTIONS.addFirst(demographicData -> queryMatchProbabilisticBlock(demographicData,
//                                                                                                   0,
//                                                                                                   PROBABILISTIC_MATCH_BLOCK_QUERIES));
   }


   private DgraphQueries() {
   }

   private static DgraphSourceIds runSourceIdQuery(
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(GET_SOURCE_ID, vars);
         if (!StringUtils.isBlank(json)) {
            return OBJECT_MAPPER.readValue(json, DgraphSourceIds.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new DgraphSourceIds(List.of());
   }

   private static List<ExpandedSourceId> runReverseGoldenRecordListFromSourceId(
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(GET_EXPANDED_SOURCE_ID_LIST, vars);
         if (!StringUtils.isBlank(json)) {
            return new JsonNodeReverseGoldenRecordListFromSourceId(json).toExpandedSourceId();
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return List.of();
   }

   /**
    * Run interactions query dgraph interactions.
    *
    * @param query the query
    * @param vars  the vars
    * @return the dgraph interactions
    */
   static DgraphInteractions runInteractionsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return OBJECT_MAPPER.readValue(json, DgraphInteractions.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return new DgraphInteractions(List.of());
   }

   /**
    * Runfilter gids query dgraph paginated uid list.
    *
    * @param query the query
    * @param vars  the vars
    * @return the dgraph paginated uid list
    */
   static DgraphPaginatedUidList runfilterGidsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return OBJECT_MAPPER.readValue(json, DgraphPaginatedUidList.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphPaginatedUidList(List.of());
   }

   /**
    * Run filter gids with interaction count query dgraph pagination uid list with interaction count.
    *
    * @param query the query
    * @param vars  the vars
    * @return the dgraph pagination uid list with interaction count
    */
   static DgraphPaginationUidListWithInteractionCount runFilterGidsWithInteractionCountQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return OBJECT_MAPPER.readValue(json, DgraphPaginationUidListWithInteractionCount.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphPaginationUidListWithInteractionCount(List.of(), List.of());
   }

   /**
    * Run golden records query list.
    *
    * @param query the query
    * @param vars  the vars
    * @return the list
    */
   static List<GoldenRecord> runGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return new JsonNodeGoldenRecords(json).toGoldenRecordList();
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }


   /**
    * Run expanded golden records query dgraph expanded golden records.
    *
    * @param query the query
    * @param vars  the vars
    * @return the dgraph expanded golden records
    */
   static DgraphExpandedGoldenRecords runExpandedGoldenRecordsQuery(
         final String query,
         final Map<String, String> vars) {
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, vars);
         if (!StringUtils.isBlank(json)) {
            return OBJECT_MAPPER.readValue(json, DgraphExpandedGoldenRecords.class);
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return new DgraphExpandedGoldenRecords(List.of());
   }

   /**
    * Find interaction interaction.
    *
    * @param interactionId the interaction id
    * @return the interaction
    */
   static Interaction findInteraction(final String interactionId) {
      if (StringUtils.isBlank(interactionId)) {
         return null;
      }
      final var vars = Map.of("$uid", interactionId);
      final var interactionList = runInteractionsQuery(DGRAPH_CONFIG.queryGetInteractionByUid, vars).all();
      if (AppUtils.isNullOrEmpty(interactionList)) {
         return null;
      }
      return DeprecatedCustomFunctions.toInteractionWithScore(interactionList.getFirst()).interaction();
   }

   /**
    * Find dgraph golden record golden record.
    *
    * @param goldenId the golden id
    * @return the golden record
    */
   static GoldenRecord findDgraphGoldenRecord(final String goldenId) {
      if (StringUtils.isBlank(goldenId)) {
         return null;
      }
      final var vars = Map.of("$uid", goldenId);
      final var goldenRecordList = runGoldenRecordsQuery(DGRAPH_CONFIG.queryGetGoldenRecordByUid, vars);

      if (AppUtils.isNullOrEmpty(goldenRecordList)) {
         LOGGER.warn("No goldenRecord for {}", goldenId);
         return null;
      }
      return goldenRecordList.getFirst();
   }


   /**
    * Find expanded golden ids list.
    *
    * @param goldenId the golden id
    * @return the list
    */
   static List<String> findExpandedGoldenIds(final String goldenId) {
      try {
         final Map<String, String> map = new HashMap<>();
         map.put("$gid", goldenId);
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(RECORD_GOLDEN_UID_INTERACTION_UID_LIST, map);
         final var response = OBJECT_MAPPER.readValue(json, DgraphUidUidList.class);
         if (AppUtils.isNullOrEmpty(response.list())) {
            return List.of();
         }
         if (response.list().size() == 1 && !AppUtils.isNullOrEmpty(response.list().getFirst().list())) {
            return response.list().getFirst().list().stream().map(DgraphUid::uid).toList();
         }
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   /**
    * Gets golden ids.
    *
    * @return the golden ids
    */
   static List<String> getGoldenIds() {
      final String query = """
                           query recordGoldenId() {
                             list(func: type(GoldenRecord)) {
                               uid
                             }
                           }""";
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = OBJECT_MAPPER.readValue(json, DgraphUidList.class);
         final var list = new ArrayList<String>();
         response.list().forEach(x -> list.add(x.uid()));
         return list;
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return List.of();
   }

   /**
    * Fetch golden ids list.
    *
    * @param offset the offset
    * @param length the length
    * @return the list
    */
   static List<String> fetchGoldenIds(
         final long offset,
         final long length) {
      final String query = String.format(Locale.ROOT, """
                                                      query recordGoldenIds() {
                                                        list(func: type(GoldenRecord), offset: %d, first: %d) {
                                                          uid
                                                        }
                                                      }""", offset, length);
      try {
         final var json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
         final var response = OBJECT_MAPPER.readValue(json, DgraphUidList.class);
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
         final var response = OBJECT_MAPPER.readValue(json, DgraphCountList.class);
         return response.list().getFirst().count();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }
      return 0L;
   }

   /**
    * Count golden records long.
    *
    * @return the long
    */
   static long countGoldenRecords() {
      final var query = """
                        query recordCount() {
                          list(func: type(GoldenRecord)) {
                            count(uid)
                          }
                        }""";
      return getCount(query);
   }

   /**
    * Count golden record entities long.
    *
    * @param goldenId the golden id
    * @return the long
    */
   static long countGoldenRecordEntities(final String goldenId) {
      final var query = String.format(Locale.ROOT, """
                                                   query recordCount() {
                                                     list(func: uid(%s)) {
                                                       count: count(GoldenRecord.interactions)
                                                     }
                                                   }""", goldenId);
      return getCount(query);
   }

   /**
    * Count interactions long.
    *
    * @return the long
    */
   static long countInteractions() {
      final var query = """
                        query recordCount() {
                          list(func: type(Interaction)) {
                            count(uid)
                          }
                        }""";
      return getCount(query);
   }

   private static LinkedList<GoldenRecord> deterministicSelectGoldenRecords(
         final List<PartialFunction<DemographicData, List<GoldenRecord>>> queryFunctions,
         final DemographicData demographicData) {
      final LinkedList<GoldenRecord> candidateGoldenRecords = new LinkedList<>();
      for (PartialFunction<DemographicData, List<GoldenRecord>> queryFunction : queryFunctions) {
         final var block = queryFunction.apply(demographicData);
         if (!AppUtils.isNullOrEmpty(block)) {
            candidateGoldenRecords.addAll(block);
            return candidateGoldenRecords;
         }
      }
      return candidateGoldenRecords;
   }

   /**
    * Find expanded interactions list.
    *
    * @param ids the ids
    * @return the list
    */
   static List<ExpandedInteraction> findExpandedInteractions(final List<String> ids) {
      final String query = String.format(Locale.ROOT, DGRAPH_CONFIG.queryGetExpandedInteractions, String.join(",", ids));
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         return OBJECT_MAPPER.readValue(json, DgraphExpandedInteractions.class)
                             .all()
                             .stream()
                             .map(DeprecatedCustomFunctions::toExpandedInteraction)
                             .toList();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   /**
    * Find golden records either.
    *
    * @param ids the ids
    * @return the either
    */
   static Either<MpiGeneralError, List<GoldenRecord>> findGoldenRecords(final List<String> ids) {
      final var idListAsString = String.join(",", ids);
      final String query = String.format(Locale.ROOT, DGRAPH_CONFIG.queryGetGoldenRecords, idListAsString);
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         return Either.right(new JsonNodeGoldenRecords(json).toGoldenRecordList());
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return Either.left(new MpiServiceError.CRGidDoesNotExistError(idListAsString));
      }
   }

   /**
    * Gets expanded golden records.
    *
    * @param ids the ids
    * @return the expanded golden records
    */
   static List<ExpandedGoldenRecord> getExpandedGoldenRecords(final List<String> ids) {
      final String query = DGRAPH_CONFIG.queryGetExpandedGoldenRecords.formatted(String.join(",", ids));
      final String json = DgraphClient.getInstance().executeReadOnlyTransaction(query, null);
      try {
         return OBJECT_MAPPER.readValue(json, DgraphExpandedGoldenRecords.class)
                             .all()
                             .stream()
                             .map(DeprecatedCustomFunctions::toExpandedGoldenRecord)
                             .toList();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

   private static List<String> getSimpleSearchQueryArguments(final List<ApiModels.ApiSearchParameter> parameters) {
      List<String> args = new ArrayList<>();
      for (ApiModels.ApiSearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = AppUtils.camelToSnake(param.fieldName());
            args.add(String.format(Locale.ROOT, "$%s: string", fieldName));
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
               String fieldName = AppUtils.camelToSnake(param.fieldName());
               args.add(String.format(Locale.ROOT, "$%s_%d: string", fieldName, i));
            }
         }
      }
      return args;
   }

   private static HashMap<String, String> getSimpleSearchQueryVariables(final List<ApiModels.ApiSearchParameter> parameters) {
      final var vars = new HashMap<String, String>();
      for (ApiModels.ApiSearchParameter param : parameters) {
         if (!param.value().isEmpty()) {
            String fieldName = AppUtils.camelToSnake(param.fieldName());
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
               String fieldName = AppUtils.camelToSnake(param.fieldName());
               String value = param.value();
               vars.put(String.format(Locale.ROOT, "$%s_%d", fieldName, i), value);
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
            String fieldName = AppUtils.camelToSnake(param.fieldName());
            Integer distance = param.distance();
            String value = param.value();
            switch (distance) {
               case -1 -> {
                  if (value.contains("_")) {
                     gqlFilters.add("ge(" + recordType + "." + fieldName + ", \"" + value.substring(0, value.indexOf("_"))
                                    + "\") AND le("
                                    + recordType + "." + fieldName + ", \"" + value.substring(value.indexOf("_") + 1) + "\")");
                  } else {
                     gqlFilters.add("le(" + recordType + "." + fieldName + ", \"" + value + "\")");
                  }
               }
               case 0 -> {
                  if (value.contains("_")) {
                     gqlFilters.add(
                           "eq(" + recordType + "." + fieldName + ", \"" + value.substring(0, value.indexOf("_")) + "\")");
                  } else {
                     gqlFilters.add("eq(" + recordType + "." + fieldName + ", \"" + value + "\")");
                  }
               }
               default -> gqlFilters.add("match(" + recordType + "." + fieldName + ", $" + fieldName + ", " + distance + ")");
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
               String fieldName = AppUtils.camelToSnake(param.fieldName());
               Integer distance = param.distance();
               String value = param.value();
               if (distance == 0) {
                  gqlAndCondition.add("eq(" + recordType + "." + fieldName + ", \"" + value + "\")");
               } else {
                  gqlAndCondition.add("match(" + recordType + "." + fieldName + ", $" + fieldName + "_" + i + ", " + distance + ")");
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
         final boolean sortAsc) {
      String direction = sortAsc
            ? "asc"
            : "desc";
      String sort = "";
      // Sort by default is by uid
      if (sortBy != null && !sortBy.isBlank() && !sortBy.equals("uid")) {
         sort = String.format(Locale.ROOT, ", order%s: %s.%s", direction, recordType, AppUtils.camelToSnake(sortBy));
      }
      return String.format(Locale.ROOT, "func: type(%s), first: %d, offset: %d", recordType, limit, offset) + sort;
   }

   private static String getSearchQueryPagination(
         final RecordType recordType,
         final String gqlFilters) {
      return String.format(Locale.ROOT, "pagination(func: type(%s)) @filter(%s) {%ntotal: count(uid)%n}", recordType, gqlFilters);
   }

   private static DgraphExpandedGoldenRecords searchGoldenRecords(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord, offset, limit, sortBy, sortAsc);
      String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);

      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format(Locale.ROOT, "all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += DGRAPH_CONFIG.expandedGoldenRecordFieldNames;
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      return runExpandedGoldenRecordsQuery(gql, gqlVars);
   }

   /**
    * Simple search golden records dgraph expanded golden records.
    *
    * @param params  the params
    * @param offset  the offset
    * @param limit   the limit
    * @param sortBy  the sort by
    * @param sortAsc the sort asc
    * @return the dgraph expanded golden records
    */
   static DgraphExpandedGoldenRecords simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      String gqlFilters = getSimpleSearchQueryFilters(RecordType.GoldenRecord, params);
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return searchGoldenRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   /**
    * Custom search golden records dgraph expanded golden records.
    *
    * @param payloads the payloads
    * @param offset   the offset
    * @param limit    the limit
    * @param sortBy   the sort by
    * @param sortAsc  the sort asc
    * @return the dgraph expanded golden records
    */
   static DgraphExpandedGoldenRecords customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> payloads,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
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
         final Boolean sortAsc) {
      String gqlFunc = getSearchQueryFunc(RecordType.Interaction, offset, limit, sortBy, sortAsc);
      String gqlPagination = getSearchQueryPagination(RecordType.Interaction, gqlFilters);
      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format(Locale.ROOT, "all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += DGRAPH_CONFIG.interactionFieldNames;
      gql += "}\n";
      gql += gqlPagination;
      gql += "}";

      return runInteractionsQuery(gql, gqlVars);
   }

   private static Either<DgraphPaginatedUidList, DgraphPaginationUidListWithInteractionCount> filterGidsFunc(
         final String gqlFilters,
         final List<String> gqlArgs,
         final HashMap<String, String> gqlVars,
         final PaginationOptions paginationOptions,
         final Boolean getInteractionCount) {
      String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord,
                                          paginationOptions.offset(),
                                          paginationOptions.limit(),
                                          paginationOptions.sortBy(),
                                          paginationOptions.sortAsc());
      String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);
      String gqlPaginationCount = Boolean.TRUE.equals(getInteractionCount)
            ? String.format(Locale.ROOT, """
                                           var(func: type(GoldenRecord)) @filter(%s){
                                             a as count(GoldenRecord.interactions)}
                                               interactionCount(){
                                                 total: sum(val(a))
                                                 }
                                         """, gqlFilters)
            : "";
      String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
      gql += String.format(Locale.ROOT, "all(%s) @filter(%s)", gqlFunc, gqlFilters);
      gql += "{\n";
      gql += "uid";
      gql += "}\n";
      gql += gqlPaginationCount;
      gql += gqlPagination;
      gql += "}";

      return Boolean.TRUE.equals(getInteractionCount)
            ? Either.right(runFilterGidsWithInteractionCountQuery(gql, gqlVars))
            : Either.left(runfilterGidsQuery(gql, gqlVars));
   }

   /**
    * Filter gids with params either.
    *
    * @param params              the params
    * @param createdAt           the created at
    * @param paginationOptions   the pagination options
    * @param getInteractionCount the get interaction count
    * @return the either
    */
   static Either<DgraphPaginatedUidList, DgraphPaginationUidListWithInteractionCount> filterGidsWithParams(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions,
         final Boolean getInteractionCount) {
      String dateFilter = String.format(Locale.ROOT, "le(GoldenRecord.aux_date_created,\"%s\")", createdAt);
      String filter = getSimpleSearchQueryFilters(RecordType.GoldenRecord, params);
      String gqlFilters = !filter.isEmpty()
            ? String.format(Locale.ROOT, "%s AND %s", filter, dateFilter)
            : dateFilter;
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);
      return filterGidsFunc(gqlFilters, gqlArgs, gqlVars, paginationOptions, getInteractionCount);
   }

   /**
    * Simple search interactions dgraph interactions.
    *
    * @param params  the params
    * @param offset  the offset
    * @param limit   the limit
    * @param sortBy  the sort by
    * @param sortAsc the sort asc
    * @return the dgraph interactions
    */
   static DgraphInteractions simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      String gqlFilters = getSimpleSearchQueryFilters(RecordType.Interaction, params);
      List<String> gqlArgs = getSimpleSearchQueryArguments(params);
      HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

      return searchInteractions(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   /**
    * Custom search interactions dgraph interactions.
    *
    * @param payloads the payloads
    * @param offset   the offset
    * @param limit    the limit
    * @param sortBy   the sort by
    * @param sortAsc  the sort asc
    * @return the dgraph interactions
    */
   static DgraphInteractions customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> payloads,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      String gqlFilters = getCustomSearchQueryFilters(RecordType.Interaction, payloads);
      List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
      HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

      return searchInteractions(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
   }

   /**
    * Find source id list list.
    *
    * @param facility the facility
    * @param patient  the patient
    * @return the list
    */
   static List<CustomSourceId> findSourceIdList(
         final String facility,
         final String patient) {
      if (StringUtils.isBlank(facility) || StringUtils.isBlank(patient)) {
         return List.of();
      }
      final var map = new HashMap<String, String>();
      map.put("$facility_id", facility);
      map.put("$patient_id", patient);
      return runSourceIdQuery(map).all().stream().map(DgraphSourceId::toSourceId).toList();
   }

   /**
    * Find expanded source id list list.
    *
    * @param facility the facility
    * @param patient  the patient
    * @return the list
    */
   static List<ExpandedSourceId> findExpandedSourceIdList(
         final String facility,
         final String patient) {
      if (StringUtils.isBlank(facility) || StringUtils.isBlank(patient)) {
         return List.of();
      }
      final var map = new HashMap<String, String>();
      map.put("$facility_id", facility);
      map.put("$patient_id", patient);
      return runReverseGoldenRecordListFromSourceId(map);
   }

   /**
    * Find golden records either.
    *
    * @param req the req
    * @return the either
    */
   static Either<MpiGeneralError, List<GoldenRecord>> findGoldenRecords(final ApiModels.ApiCrFindRequest req) {
      final var setFunctions = new HashSet<String>();
      setFunctions.add("eq");
      setFunctions.add("match");
      final var setOperators = new HashSet<String>();
      setOperators.add("and");
      setOperators.add("or");
      try {
         final var operand = req.operand();
         final var queryBuilder =
               new StringBuilder("query query_1 ($").append(AppUtils.camelToSnake(operand.name())).append(":string");
         if (req.operands() != null) {
            for (ApiModels.ApiCrFindRequest.ApiLogicalOperand op2 : req.operands()) {
               queryBuilder.append(", $").append(AppUtils.camelToSnake(op2.operand().name())).append(":string");
            }
         }
         queryBuilder.append(") {\n\n");
         char alias = 'A';
         if (!setFunctions.contains(operand.fn())) {
            throw new InvalidFunctionException(String.format("Invalid function: %s", operand.fn()));
         }
         if (operand.fn().equals("match")) {
            if (operand.distance() == null) {
               throw new InvalidFunctionException("no distance parameter");
            } else if (operand.distance() < 0 || operand.distance() > 5) {
               throw new InvalidFunctionException(String.format("distance error: 0 < %d <= 5", operand.distance()));
            }
         }
         queryBuilder.append("  var(func:type(GoldenRecord)) @filter(")
                     .append(operand.fn())
                     .append("(GoldenRecord.")
                     .append(AppUtils.camelToSnake(operand.name()))
                     .append(", $")
                     .append(AppUtils.camelToSnake(operand.name()))
                     .append(operand.fn().equals("match")
                                   ? String.format(Locale.ROOT, ", %d", operand.distance())
                                   : "")
                     .append(")) {\n    ")
                     .append(alias)
                     .append(" as uid\n  }\n\n");

         if (req.operands() != null) {
            for (ApiModels.ApiCrFindRequest.ApiLogicalOperand o : req.operands()) {
               if (!setFunctions.contains(o.operand().fn())) {
                  throw new InvalidFunctionException(String.format("Invalid function: %s", o.operand().fn()));
               }
               if (o.operand().fn().equals("match")) {
                  if (o.operand().distance() == null) {
                     throw new InvalidFunctionException("no distance parameter");
                  } else if (o.operand().distance() < 0 || o.operand().distance() > 5) {
                     throw new InvalidFunctionException(String.format("distance error: 0 < %d <= 5", o.operand().distance()));
                  }
               }
               if (!setOperators.contains(o.operator())) {
                  throw new InvalidOperatorException(String.format("Invalid operator: %s", o.operator()));
               }
               queryBuilder.append("  var(func:type(GoldenRecord)) @filter(")
                           .append(o.operand().fn())
                           .append("(GoldenRecord.")
                           .append(AppUtils.camelToSnake(o.operand().name()))
                           .append(", $")
                           .append(AppUtils.camelToSnake(o.operand().name()))
                           .append(o.operand().fn().equals("match")
                                         ? String.format(Locale.ROOT, ", %d", o.operand().distance())
                                         : "")
                           .append(")) {\n    ")
                           .append(++alias)
                           .append(" as uid\n  }\n\n");
            }
         }

         alias = 'A';
         queryBuilder.append("  all(func:type(GoldenRecord)) @filter(uid(A)");
         if (req.operands() != null) {
            for (ApiModels.ApiCrFindRequest.ApiLogicalOperand o : req.operands()) {
               queryBuilder.append(" ").append(o.operator()).append(" uid(").append(++alias).append(")");
            }
         }
         queryBuilder.append(") {\n").append(DGRAPH_CONFIG.goldenRecordFieldNames).append("  }\n}\n");
         final var query = queryBuilder.toString();
         final var map = new HashMap<String, String>();
         map.put("$" + AppUtils.camelToSnake(operand.name()), operand.value());
         for (var o : req.operands()) {
            map.put("$" + AppUtils.camelToSnake(o.operand().name()), o.operand().value());
         }

         final var dgraphGoldenRecords = runGoldenRecordsQuery(query, map);
         return Either.right(dgraphGoldenRecords);
      } catch (InvalidFunctionException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InvalidFunctionError(e.getMessage()));
      } catch (InvalidOperatorException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InvalidOperatorError(e.getLocalizedMessage()));
      }
   }

   private static void mergeCandidates(
         final List<GoldenRecord> goldenRecords,
         final List<GoldenRecord> block) {
      if (!block.isEmpty()) {
         block.forEach(candidate -> {
            var found = false;
            for (GoldenRecord goldenRecord : goldenRecords) {
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

   /**
    * Find link candidates list.
    *
    * @param interaction the interaction
    * @return the list
    */
   static List<GoldenRecord> findLinkCandidates(
         final DemographicData interaction) {
      var result = deterministicSelectGoldenRecords(DETERMINISTIC_LINK_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      if (!PROBABILISTIC_LINK_BLOCK_FUNCTIONS.isEmpty()) {
         final var candidates = PROBABILISTIC_LINK_BLOCK_FUNCTIONS.getFirst().apply(interaction);
         if (!candidates.isEmpty()) {
            mergeCandidates(result, candidates);
         }
      }
      return result;
   }

   /**
    * Find match candidates list.
    *
    * @param interaction the interaction
    * @return the list
    */
   static List<GoldenRecord> findMatchCandidates(
         final DemographicData interaction) {
      var result = deterministicSelectGoldenRecords(DETERMINISTIC_MATCH_FUNCTIONS, interaction);
      if (!result.isEmpty()) {
         return result;
      }
      result = new LinkedList<>();
      if (!PROBABILISTIC_MATCH_BLOCK_FUNCTIONS.isEmpty()) {
         final var candidates = PROBABILISTIC_MATCH_BLOCK_FUNCTIONS.getFirst().apply(interaction);
         if (!candidates.isEmpty()) {
            mergeCandidates(result, candidates);
         }
      }
      return result;
   }

   private static List<GoldenRecord> queryMatchProbabilisticBlock(
         final DemographicData demographicData,
         final int ruleNumber,
         final String[] queries) {
      if (LINKER_CONFIG.probabilisticLinkFields.isEmpty()) {
         return List.of();
      }
      final Map<String, String> map = new HashMap<>();
      JSON_CONFIG.rules().matchNotification().probabilistic().get(ruleNumber).vars().forEach(scFieldName -> {
         final var ccFieldName = AppUtils.snakeToCamelCase(scFieldName);
         final var fieldIdx = FIELDS_CONFIG.findIndexOfDemographicField(ccFieldName);
         final var fieldValue = demographicData.fields.get(fieldIdx).value();
         map.put("$" + scFieldName,
                 StringUtils.isNotBlank(fieldValue)
                       ? fieldValue
                       : EMPTY_FIELD_SENTINEL);
      });
      return runGoldenRecordsQuery(queries[ruleNumber], map);
   }


   private static List<GoldenRecord> queryLinkProbabilisticBlock(
         final DemographicData demographicData,
         final int ruleNumber,
         final String[] queries) {
      if (LINKER_CONFIG.probabilisticLinkFields.isEmpty()) {
         return List.of();
      }
      final Map<String, String> map = new HashMap<>();
      JSON_CONFIG.rules().link().probabilistic().get(ruleNumber).vars().forEach(scFieldName -> {
         final var ccFieldName = AppUtils.snakeToCamelCase(scFieldName);
         final var fieldIdx = FIELDS_CONFIG.findIndexOfDemographicField(ccFieldName);
         final var fieldValue = demographicData.fields.get(fieldIdx).value();
         map.put("$" + scFieldName,
                 StringUtils.isNotBlank(fieldValue)
                       ? fieldValue
                       : EMPTY_FIELD_SENTINEL);
      });
      return runGoldenRecordsQuery(queries[ruleNumber], map);
   }

   private static List<GoldenRecord> queryLinkDeterministic(
         final DemographicData demographicData,
         final int ruleNumber,
         final String[] queries) {
      if (!LINKER_CONFIG.canApplyDeterministicLinking(LINKER_CONFIG.deterministicLinkPrograms.get(ruleNumber),
                                                      demographicData)) {
         return List.of();
      }
      final Map<String, String> map = new HashMap<>();
      JSON_CONFIG.rules().link().deterministic().get(ruleNumber).vars().forEach(scFieldName -> {
         final var ccFieldName = AppUtils.snakeToCamelCase(scFieldName);
         final var fieldIdx = FIELDS_CONFIG.findIndexOfDemographicField(ccFieldName);
         final var fieldValue = demographicData.fields.get(fieldIdx).value();
         map.put("$" + scFieldName,
                 StringUtils.isNotBlank(fieldValue)
                       ? fieldValue
                       : EMPTY_FIELD_SENTINEL);
      });
      return runGoldenRecordsQuery(queries[ruleNumber], map);
   }

   private static List<GoldenRecord> queryMatchDeterministic(
         final DemographicData demographicData,
         final int ruleNumber,
         final String[] queries) {
      if (!LINKER_CONFIG.canApplyDeterministicLinking(LINKER_CONFIG.deterministicMatchPrograms.get(ruleNumber),
                                                      demographicData)) {
         return List.of();
      }
      final Map<String, String> map = new HashMap<>();
      JSON_CONFIG.rules().matchNotification().deterministic().get(ruleNumber).vars().forEach(scFieldName -> {
         final var ccFieldName = AppUtils.snakeToCamelCase(scFieldName);
         final var fieldIdx = FIELDS_CONFIG.findIndexOfDemographicField(ccFieldName);
         final var fieldValue = demographicData.fields.get(fieldIdx).value();
         map.put("$" + scFieldName,
                 StringUtils.isNotBlank(fieldValue)
                       ? fieldValue
                       : EMPTY_FIELD_SENTINEL);
      });
      return runGoldenRecordsQuery(queries[ruleNumber], map);
   }


   private static class InvalidFunctionException extends Exception {
      /**
       * Instantiates a new Invalid function exception.
       *
       * @param errorMessage the error message
       */
      InvalidFunctionException(final String errorMessage) {
         super(errorMessage);
      }
   }

   private static class InvalidOperatorException extends Exception {
      /**
       * Instantiates a new Invalid operator exception.
       *
       * @param errorMessage the error message
       */
      InvalidOperatorException(final String errorMessage) {
         super(errorMessage);
      }
   }

}
