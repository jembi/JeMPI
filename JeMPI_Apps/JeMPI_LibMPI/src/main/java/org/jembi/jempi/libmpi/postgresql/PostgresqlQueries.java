package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.jembi.jempi.libmpi.postgresql.PostgresqlMutations.*;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class PostgresqlQueries {

   private static final Logger LOGGER = LogManager.getLogger(PostgresqlQueries.class);

   private PostgresqlQueries() {
   }

   public static Float getScore(
         final UUID gid,
         final UUID eid) {
      final var sql = String.format("select facet from %s where source = ? and dest = ?;", TABLE_EDGES_GID2EID);
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, gid, Types.OTHER);
         stmt.setObject(2, eid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var json = rs.getString("facet");
            final var facetScore = OBJECT_MAPPER.readValue(json, FacetScore.class);
            return facetScore.score();
         }
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return null;
   }

   private static Integer countNodeType(final Node.NodeType nodeType) {
      final var sql = String.format("select count(*) from %s where type = '%s';", TABLE_NODES, nodeType.name());
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeQuery(sql);
         if (rs.next()) {
            return rs.getInt(1);
         }
         return 0;
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return -1;
      }
   }

   public static List<UUID> getGoldenIds() {
      final List<UUID> result = new ArrayList<>();
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeQuery(String.format("select id from %s;", TABLE_NODES_GOLDEN_RECORD));
         while (rs.next()) {
            result.add(UUID.fromString(rs.getString(1)));
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return result;
   }

   public static Integer countEncounters() {
      return countNodeType(Node.NodeType.ENCOUNTER);
   }

   public static Integer countGoldenRecords() {
      return countNodeType(Node.NodeType.GOLDEN_RECORD);
   }

   public static List<NodeSourceId> findSourceId(
         final String facility,
         final String patient) {
      final var sql = String.format("select * from %s where fields->>'facility' = ? and fields->>'patient' = ?;",
                                    TABLE_NODES_SOURCE_ID);
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setString(1, facility);
         stmt.setString(2, patient);
         final var rs = stmt.executeQuery();
         final var list = new ArrayList<NodeSourceId>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            list.add(new NodeSourceId(Node.NodeType.SOURCE_ID,
                                      UUID.fromString(id),
                                      OBJECT_MAPPER.readValue(json, NodeSourceId.SourceIdData.class)));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   private static List<NodeSourceId> runQuery(final PreparedStatement stmt) throws SQLException, JsonProcessingException {
      final var rs = stmt.executeQuery();
      final var list = new ArrayList<NodeSourceId>();
      while (rs.next()) {
         final var id = rs.getString("id");
         final var json = rs.getString("fields");
         final var sourceIdData = OBJECT_MAPPER.readValue(json, NodeSourceId.SourceIdData.class);
         list.add(new NodeSourceId(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), sourceIdData));
      }
      return list;
   }

   public static List<NodeSourceId> getEncounterSourceIds(final UUID eid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select dest from %s where source = ?);
                                    """,
                                    TABLE_NODES_SOURCE_ID,
                                    TABLE_EDGES_EID2SID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, eid, Types.OTHER);
         return runQuery(stmt);
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<NodeSourceId> getGoldenRecordSourceIds(final UUID gid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select dest from %s where source = ?);
                                    """,
                                    TABLE_NODES_SOURCE_ID,
                                    TABLE_EDGES_GID2SID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, gid, Types.OTHER);
         return runQuery(stmt);
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<NodeGoldenRecord> getGoldenRecordsOfEncounter(final UUID eid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select source from %s where dest = ?);
                                    """,
                                    TABLE_NODES_GOLDEN_RECORD,
                                    TABLE_EDGES_GID2EID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, eid, Types.OTHER);
         final var rs = stmt.executeQuery();
         final var list = new ArrayList<NodeGoldenRecord>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var goldenRecordData = new NodeGoldenRecord.GoldenRecordData(OBJECT_MAPPER.readValue(json,
                                                                                                       CustomDemographicData.class));
            list.add(new NodeGoldenRecord(Node.NodeType.valueOf(rs.getString("type")),
                                          UUID.fromString(id),
                                          goldenRecordData));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<NodeEncounter> getGoldenRecordEncounters(final UUID gid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select dest from %s where source = ?);
                                    """,
                                    TABLE_NODES_ENCOUNTER,
                                    TABLE_EDGES_GID2EID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, gid, Types.OTHER);
         final var rs = stmt.executeQuery();
         final var list = new ArrayList<NodeEncounter>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var encounterData = new NodeEncounter.EncounterData(OBJECT_MAPPER.readValue(json, CustomDemographicData.class));
            list.add(new NodeEncounter(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), encounterData));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<GoldenRecord> findCandidates(final CustomDemographicData customDemographicData) {
      final var sql = String.format("""
                                    select *
                                    from %s
                                    where fields->>'nationalId' = ?
                                    or (fields->>'givenName' = ? and
                                        fields->>'familyName' = ? and
                                        fields->>'phoneNumber' = ?)
                                    or (levenshtein(fields->>'givenName', ?) < 3)::integer +
                                       (levenshtein(fields->>'familyName', ?) < 3)::integer +
                                       (levenshtein(fields->>'city', ?) < 3)::integer >= 2
                                    or levenshtein(fields->>'phoneNumber', ?) < 3
                                    or levenshtein(fields->>'nationalId', ?) < 3;
                                    """,
                                    TABLE_NODES_GOLDEN_RECORD).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setString(1, customDemographicData.nationalId);
         stmt.setString(2, customDemographicData.givenName);
         stmt.setString(3, customDemographicData.familyName);
         stmt.setString(4, customDemographicData.phoneNumber);
         stmt.setString(5, customDemographicData.givenName);
         stmt.setString(6, customDemographicData.familyName);
         stmt.setString(7, customDemographicData.city);
         stmt.setString(8, customDemographicData.phoneNumber);
         stmt.setString(9, customDemographicData.nationalId);

         final var rs = stmt.executeQuery();
         final var list = new ArrayList<GoldenRecord>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var goldenRecordData = new NodeGoldenRecord.GoldenRecordData(OBJECT_MAPPER.readValue(json,
                                                                                                       CustomDemographicData.class));
            list.add(new GoldenRecord(id,
                                      null,
                                      goldenRecordData));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static NodeGoldenRecord getGoldenRecord(final UUID gid) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          select * from %s where id = ?;
                          """,
                          TABLE_NODES_GOLDEN_RECORD).stripIndent())) {
         stmt.setObject(1, gid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var id = rs.getString("id");
            final var goldenRecordData = new NodeGoldenRecord.GoldenRecordData(OBJECT_MAPPER.readValue(rs.getString("fields"),
                                                                                                       CustomDemographicData.class));
            return new NodeGoldenRecord(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), goldenRecordData);
         }
         return null;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   public static NodeEncounter getEncounter(final UUID eid) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          select * from %s
                          where id = ?;
                          """,
                          TABLE_NODES_ENCOUNTER))) {
         stmt.setObject(1, eid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var id = rs.getString("id");
            final var encounterData = new NodeEncounter.EncounterData(OBJECT_MAPPER.readValue(rs.getString("fields"),
                                                                                              CustomDemographicData.class));
            return new NodeEncounter(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), encounterData);
         }
         return null;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   public static NodeSourceId getSourceId(final UUID sid) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          select * from %s where id = ?;
                          """,
                          TABLE_NODES_SOURCE_ID).stripIndent())) {
         stmt.setObject(1, sid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var id = rs.getString("id");
            final var sourceIdData =
                  OBJECT_MAPPER.readValue(rs.getString("fields"), NodeSourceId.SourceIdData.class);
            return new NodeSourceId(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), sourceIdData);
         }
         return null;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

}
