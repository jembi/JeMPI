package org.jembi.jempi.libmpi.dgraph;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

final class Queries {

   static final String EMPTY_FIELD_SENTINEL = "EMPTY_FIELD_SENTINEL";
   private static final Logger LOGGER = LogManager.getLogger(Queries.class);

   ///////////////////////////////////////////////////////////////////////////////////////////
   private final static String url = "jdbc:postgresql://192.168.0.195:5432/akka_test";
//   private final static String url = "jdbc:postgresql://172.17.0.3/test?user=mahao&password=12345&ssl=true";
   private final static String user = "postgres";
   private final static String password = "12345";
   private static final String QUERY = "select id,given_name,family_name from patients where id =?";
   private static final String SELECT_ALL_QUERY = "select given_name from patients";

   ///////////////////////////////////////////////////////////////////////////////////////////////

   private Queries() {}

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

   static LibMPIDGraphEntityList runEntityQuery(final String query, final Map<String, String> vars) {
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

   static LibMPIGoldenRecordList runGoldenRecordQuery(final String query, final Map<String, String> vars) {
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
      final var dgraphEntityList = runEntityQuery(CustomLibMPIConstants.QUERY_GET_ENTITY_BY_UID, vars).all();
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
      final var goldenRecordList = runGoldenRecordQuery(CustomLibMPIConstants.QUERY_GET_GOLDEN_RECORD_BY_UID, vars).all();

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
      final var query =
              """
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
      final var query =
              """
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
      final String query = String.format(CustomLibMPIConstants.QUERY_GET_GOLDEN_RECORD_ENTITIES, String.join(",", goldenIdList));
      final String json = Client.getInstance().executeReadOnlyTransaction(query, null);
      try {
         final var records = AppUtils.OBJECT_MAPPER.readValue(json, LibMPIExpandedGoldenRecordList.class);
         return records.all();
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
         return List.of();
      }
   }

}
