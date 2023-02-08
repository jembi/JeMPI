package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.shared.utils.RecordType;
import org.jembi.jempi.shared.utils.SimpleSearchRequestPayload;
import org.jembi.jempi.shared.utils.AppUtils;

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

    static LibMPIDGraphEntityList runPatientRecordsQuery(final String query, final Map<String, String> vars) {
        try {
            final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
            if (!StringUtils.isBlank(json)) {
                return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIDGraphEntityList.class);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return new LibMPIDGraphEntityList(List.of());
    }

    static LibMPIGoldenRecordList runGoldenRecordsQuery(final String query, final Map<String, String> vars) {
        try {
            final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
            if (!StringUtils.isBlank(json)) {
                return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIGoldenRecordList.class);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return new LibMPIGoldenRecordList(List.of());
    }

    static LibMPIExpandedGoldenRecordList runExpandedGoldenRecordsQuery(final String query, final Map<String, String> vars) {
        try {
            final var json = Client.getInstance().executeReadOnlyTransaction(query, vars);
            if (!StringUtils.isBlank(json)) {
                return AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedGoldenRecordList.class);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return new LibMPIExpandedGoldenRecordList(List.of());
    }

    static List<String> getGoldenIdListByPredicate(final String predicate, final String val) {
        if (StringUtils.isBlank(predicate) || StringUtils.isBlank(val)) {
            return Collections.emptyList();
        }
        final String query = String
                .format("""
                        query goldenIdListByPredicate() {
                          list(func: eq(%s, %s)) {
                            uid
                          }
                        }""", predicate, val);
        final String json = Client.getInstance().executeReadOnlyTransaction(query, null);
        try {
            final var response = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIUidList.class);
            final List<String> list = new ArrayList<>();
            response.list().forEach(x -> list.add(x.uid()));
            return list;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return Collections.emptyList();
    }

    static CustomEntity getDGraphEntity(final String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        final var vars = Map.of("$uid", uid);
        final var dgraphEntityList = runPatientRecordsQuery(CustomLibMPIConstants.QUERY_GET_ENTITY_BY_UID, vars).all();
        if (AppUtils.isNullOrEmpty(dgraphEntityList)) {
            return null;
        }
        return dgraphEntityList.get(0).toMpiEntity().entity();
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

    static List<String> getGoldenIdEntityIdList(final String uid) {
        final String query = String
                .format("""
                        query recordGoldenIdEntityIdList() {
                            list(func: uid(%s)) {
                                uid
                                list: GoldenRecord.entity_list {
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
                            count: count(GoldenRecord.entity_list)
                          }
                        }""", uid);
        return getCount(query);
    }

    static long countEntities() {
        final var query = """
                query recordCount() {
                  list(func: type(Entity)) {
                    count(uid)
                  }
                }""";
        return getCount(query);
    }

    static LinkedList<CustomLibMPIGoldenRecord> deterministicFilter(final CustomEntity customEntity) {
        final LinkedList<CustomLibMPIGoldenRecord> candidateGoldenRecords = new LinkedList<>();
        var block = CustomLibMPIQueries.queryDeterministicGoldenRecordCandidates(customEntity);
        if (!block.all().isEmpty()) {
            final List<CustomLibMPIGoldenRecord> list = block.all();
            if (!AppUtils.isNullOrEmpty(list)) {
                candidateGoldenRecords.addAll(list);
            }
        }
        return candidateGoldenRecords;
    }

    static List<CustomLibMPIExpandedGoldenRecord> getExpandedGoldenRecordList(final List<String> goldenIdList) {
        final String query = String.format(CustomLibMPIConstants.QUERY_GET_GOLDEN_RECORD_ENTITIES,
                String.join(",", goldenIdList));
        final String json = Client.getInstance().executeReadOnlyTransaction(query, null);
        try {
            final var records = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedGoldenRecordList.class);
            return records.all();
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage());
            return List.of();
        }
    }

    static String camelToSnake(String str) {
        return str.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
    }

    static <T> List<String> getRecordFieldNamesByType(RecordType recordType) {
        List<String> fieldNames = new ArrayList<String>();
        Class C = recordType == RecordType.GoldenRecord ? CustomGoldenRecord.class : CustomEntity.class;
        Field[] fields = C.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName() == "uid" ? "uid" : recordType + "." + camelToSnake(field.getName()));
        }
        return fieldNames;
    }

    static List<String> getSimpleSearchQueryArguments(List<SimpleSearchRequestPayload.SearchParameter> parameters) {
        List<String> args = new ArrayList<String>();
        for (int i = 0; i < parameters.size(); i++) {
            SimpleSearchRequestPayload.SearchParameter param = parameters.get(i);
            if (!param.value().isEmpty()) {
                String fieldName = camelToSnake(param.fieldName());
                args.add(String.format("$%s: string", fieldName));
            }
        }
        return args;
    }
    static List<String> getCustomSearchQueryArguments(List<SimpleSearchRequestPayload> payloads) {
        List<String> args = new ArrayList<String>();
        for (int i = 0; i < payloads.size(); i++) {
            List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
            for (int j = 0; j < parameters.size(); j++) {
                SimpleSearchRequestPayload.SearchParameter param = parameters.get(j);
                if (!param.value().isEmpty()) {
                    String fieldName = camelToSnake(param.fieldName());
                    args.add(String.format("$%s_%d: string", fieldName, i));
                }
            }
        }
        return args;
    }

    static HashMap<String, String> getSimpleSearchQueryVariables(List<SimpleSearchRequestPayload.SearchParameter> parameters) {
        HashMap<String, String> vars = new HashMap();
        for (int i = 0; i < parameters.size(); i++) {
            SimpleSearchRequestPayload.SearchParameter param = parameters.get(i);
            if (!param.value().isEmpty()) {
                String fieldName = camelToSnake(param.fieldName());
                String value = param.value();
                vars.put("$" + fieldName, value);
            }
        }
        return vars;
    }

    static HashMap<String, String> getCustomSearchQueryVariables(List<SimpleSearchRequestPayload> payloads) {
        HashMap<String, String> vars = new HashMap();
        for (int i = 0; i < payloads.size(); i++) {
            List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
            for (int j = 0; j < parameters.size(); j++) {
                SimpleSearchRequestPayload.SearchParameter param = parameters.get(j);
                if (!param.value().isEmpty()) {
                    String fieldName = camelToSnake(param.fieldName());
                    String value = param.value();
                    vars.put(String.format("$%s_%d", fieldName, i), value);
                }
            }
        }
        return vars;
    }

    static String getSimpleSearchQueryFilters(RecordType recordType, List<SimpleSearchRequestPayload.SearchParameter> parameters) {
        List<String> gqlFilters = new ArrayList<String>();
        for (int i = 0; i < parameters.size(); i++) {
            SimpleSearchRequestPayload.SearchParameter param = parameters.get(i);
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
        if (gqlFilters.size() > 0) {
            return String.join(" AND ", gqlFilters);
        }
        return "";
    }
    static String getCustomSearchQueryFilters(RecordType recordType, List<SimpleSearchRequestPayload> payloads) {
        List<String> gqlOrCondition = new ArrayList<String>();
        for (int i = 0; i < payloads.size(); i++) {
            List<SimpleSearchRequestPayload.SearchParameter> parameters = payloads.get(i).parameters();
            List<String> gqlAndCondition = new ArrayList<String>();
            for (int j = 0; j < parameters.size(); j++) {
                SimpleSearchRequestPayload.SearchParameter param = parameters.get(j);
                if (!param.value().isEmpty()) {
                    String fieldName = camelToSnake(param.fieldName());
                    Integer distance = param.distance();
                    if (distance == 0) {
                        gqlAndCondition.add("eq(" + recordType + "." + fieldName + ", $" + fieldName + "_" + i + ")");
                    } else {
                        gqlAndCondition.add("match(" + recordType + "." + fieldName + ", $" + fieldName + "_" + i + ", " + distance + ")");
                    }
                }
            }
            if (gqlAndCondition.size() > 0) {
                gqlOrCondition.add(String.join(" AND ", gqlAndCondition));
            }
        }
        if (gqlOrCondition.size() > 0) {
            return "(" + String.join(") OR (", gqlOrCondition) + ")";
        }
        return "";
    }

    static String getSearchQueryFunc(RecordType recordType, Integer offset, Integer limit, String sortBy, Boolean sortAsc) {
        String direction = sortAsc ? "asc" : "desc";
        String sort = "";
        // Sort by default is by uid
        if (sortBy != null && !sortBy.isBlank() && !sortBy.equals("uid")) {
            sort = String.format(", order%s: %s.%s", direction, recordType, camelToSnake(sortBy));
        }
        return String.format("func: type(%s), first: %d, offset: %d", recordType, limit, offset) + sort;
    }

    static String getSearchQueryPagination(RecordType recordType, String gqlFilters) {
        return String.format("pagination(func: type(%s)) @filter(%s) {\ntotal: count(uid)\n}", recordType, gqlFilters);
    }

    static LibMPIExpandedGoldenRecordList searchGoldenRecords(
            String gqlFilters,
            List<String> gqlArgs,
            HashMap<String, String> gqlVars,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        String gqlFunc = getSearchQueryFunc(RecordType.GoldenRecord, offset, limit, sortBy, sortAsc);
        List<String> patientRecordFieldNames = getRecordFieldNamesByType(RecordType.Entity);
        List<String> goldenRecordFieldNames = getRecordFieldNamesByType(RecordType.GoldenRecord);
        String gqlPagination = getSearchQueryPagination(RecordType.GoldenRecord, gqlFilters);

        String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
        gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
        gql += "{\n";
        gql += String.join("\n", goldenRecordFieldNames) + "\n";
        gql += RecordType.GoldenRecord + ".entity_list {\n" + String.join("\n", patientRecordFieldNames) + "}\n";
        gql += "}\n";
        gql += gqlPagination;
        gql += "}";

        LOGGER.debug("Search Golden Records Query {}", gql);
        LOGGER.debug("Search Golden Records Variables {}", gqlVars);
        return runExpandedGoldenRecordsQuery(gql, gqlVars);
    }

    static LibMPIExpandedGoldenRecordList simpleSearchGoldenRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        LOGGER.debug("Simple Search Golden Records Params {}", params);
        String gqlFilters = getSimpleSearchQueryFilters(RecordType.GoldenRecord, params);
        List<String> gqlArgs = getSimpleSearchQueryArguments(params);
        HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

        return searchGoldenRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
    }

    static LibMPIExpandedGoldenRecordList customSearchGoldenRecords(
            List<SimpleSearchRequestPayload> payloads,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        LOGGER.debug("Custom Search Golden Records Params {}", payloads);
        String gqlFilters = getCustomSearchQueryFilters(RecordType.GoldenRecord, payloads);
        List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
        HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

        return searchGoldenRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
    }

    static LibMPIDGraphEntityList searchPatientRecords(
            String gqlFilters,
            List<String> gqlArgs,
            HashMap<String, String> gqlVars,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        String gqlFunc = getSearchQueryFunc(RecordType.Entity, offset, limit, sortBy, sortAsc);
        List<String> patientRecordFieldNames = getRecordFieldNamesByType(RecordType.Entity);
        String gqlPagination = getSearchQueryPagination(RecordType.Entity, gqlFilters);

        String gql = "query search(" + String.join(", ", gqlArgs) + ") {\n";
        gql += String.format("all(%s) @filter(%s)", gqlFunc, gqlFilters);
        gql += "{\n";
        gql += String.join("\n", patientRecordFieldNames) + "\n";
        gql += RecordType.Entity + ".golden_record_list {\nuid\n}\n";
        gql += "}\n";
        gql += gqlPagination;
        gql += "}";

        LOGGER.debug("Simple Search Patient Records Query {}", gql);
        LOGGER.debug("Simple Search Patient Records Variables {}", gqlVars);
        return runPatientRecordsQuery(gql, gqlVars);
    }

    static LibMPIDGraphEntityList simpleSearchPatientRecords(
            List<SimpleSearchRequestPayload.SearchParameter> params,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        LOGGER.debug("Simple Search Patient Records Params {}", params);
        String gqlFilters = getSimpleSearchQueryFilters(RecordType.Entity, params);
        List<String> gqlArgs = getSimpleSearchQueryArguments(params);
        HashMap<String, String> gqlVars = getSimpleSearchQueryVariables(params);

        return searchPatientRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
    }

    static LibMPIDGraphEntityList customSearchPatientRecords(
            List<SimpleSearchRequestPayload> payloads,
            Integer offset,
            Integer limit,
            String sortBy,
            Boolean sortAsc
    ) {
        LOGGER.debug("Simple Search Patient Records Params {}", payloads);
        String gqlFilters = getCustomSearchQueryFilters(RecordType.Entity, payloads);
        List<String> gqlArgs = getCustomSearchQueryArguments(payloads);
        HashMap<String, String> gqlVars = getCustomSearchQueryVariables(payloads);

        return searchPatientRecords(gqlFilters, gqlArgs, gqlVars, offset, limit, sortBy, sortAsc);
    }

}
