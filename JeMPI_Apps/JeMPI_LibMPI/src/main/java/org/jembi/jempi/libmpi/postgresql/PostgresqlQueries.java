package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.CustomUniqueGoldenRecordData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
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

   private static Long countNodeType(final Node.NodeType nodeType) {
      final var sql = String.format("select count(*) from %s where type = '%s';", TABLE_NODES, nodeType.name());
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeQuery(sql);
         if (rs.next()) {
            return rs.getLong(1);
         }
         return 0L;
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return -1L;
      }
   }

   public static List<UUID> getGoldenIds() {
      final List<UUID> result = new ArrayList<>();
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeQuery(String.format("select id from %s;", TABLE_NODE_GOLDEN_RECORDS));
         while (rs.next()) {
            result.add(UUID.fromString(rs.getString(1)));
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return result;
   }

   public static Long countInteractions() {
      return countNodeType(Node.NodeType.INTERACTION);
   }

   public static Long countGoldenRecords() {
      return countNodeType(Node.NodeType.GOLDEN_RECORD);
   }

   public static List<NodeSourceId> findSourceId(
         final String facility,
         final String patient) {
      final var sql = String.format("select * from %s where fields->>'facility' = ? and fields->>'patient' = ?;",
                                    TABLE_NODE_SOURCE_IDS);
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

   public static List<NodeSourceId> getInteractionSourceIds(final UUID eid) {
      final var sql = String.format("""
                                    SELECT * FROM %s
                                    WHERE id IN (SELECT dest FROM %s WHERE source = ?);
                                    """,
                                    TABLE_NODE_SOURCE_IDS,
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
                                    TABLE_NODE_SOURCE_IDS,
                                    TABLE_EDGES_GID2SID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, gid, Types.OTHER);
         return runQuery(stmt);
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<NodeGoldenRecord> getGoldenRecordsOfInteraction(final UUID eid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select source from %s where dest = ?);
                                    """,
                                    TABLE_NODE_GOLDEN_RECORDS,
                                    TABLE_EDGES_GID2EID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, eid, Types.OTHER);
         final var rs = stmt.executeQuery();
         final var list = new ArrayList<NodeGoldenRecord>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var goldenRecordData = new CustomGoldenRecordData(OBJECT_MAPPER.readValue(json,
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

   public static List<NodeInteraction> getGoldenRecordInteractions(final UUID gid) {
      final var sql = String.format("""
                                    select * from %s
                                    where id in (select dest from %s where source = ?);
                                    """,
                                    TABLE_NODE_INTERACTIONS,
                                    TABLE_EDGES_GID2EID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, gid, Types.OTHER);
         final var rs = stmt.executeQuery();
         final var list = new ArrayList<NodeInteraction>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var interactionData = new CustomInteractionData(OBJECT_MAPPER.readValue(json, CustomDemographicData.class));
            list.add(new NodeInteraction(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), interactionData));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   private static List<GoldenRecord> findCandidatesWorker(final String sql) {
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeQuery(sql);
         final var list = new ArrayList<GoldenRecord>();
         while (rs.next()) {
            final var id = rs.getString("id");
            final var json = rs.getString("fields");
            final var goldenRecordData = new CustomGoldenRecordData(OBJECT_MAPPER.readValue(json,
                                                                                            CustomDemographicData.class));
            list.add(new GoldenRecord(id,
                                      null,
                                      new CustomUniqueGoldenRecordData(LocalDateTime.now(),
                                                                       true,
                                                                       "AUX_ID"),
                                      goldenRecordData));
         }
         return list;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Collections.emptyList();
      }
   }

   public static List<GoldenRecord> findCandidates(final CustomDemographicData customDemographicData) {
      if (!(StringUtils.isBlank(customDemographicData.phoneNumber)
            && (StringUtils.isBlank(customDemographicData.givenName)
                || StringUtils.isBlank(customDemographicData.familyName)
                || StringUtils.isBlank(customDemographicData.phoneNumber)))) {
         final var block = findCandidatesWorker(CustomQueries.sqlDeterministicCandidates(customDemographicData));
         if (!block.isEmpty()) {
            return block;
         }
      }
      return findCandidatesWorker(CustomQueries.sqlBlockedCandidates(customDemographicData));
   }

   public static NodeGoldenRecord getGoldenRecord(final UUID gid) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          select * from %s where id = ?;
                          """,
                          TABLE_NODE_GOLDEN_RECORDS).stripIndent())) {
         stmt.setObject(1, gid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var id = rs.getString("id");
            final var goldenRecordData = new CustomGoldenRecordData(OBJECT_MAPPER.readValue(rs.getString("fields"),
                                                                                            CustomDemographicData.class));
            return new NodeGoldenRecord(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), goldenRecordData);
         }
         return null;
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   static NodeInteraction getInteraction(final UUID iid) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          select * from %s
                          where id = ?;
                          """,
                          TABLE_NODE_INTERACTIONS))) {
         stmt.setObject(1, iid, Types.OTHER);
         final var rs = stmt.executeQuery();
         if (rs.next()) {
            final var id = rs.getString("id");
            final var interactionData = new CustomInteractionData(OBJECT_MAPPER.readValue(rs.getString("fields"),
                                                                                          CustomDemographicData.class));
            return new NodeInteraction(Node.NodeType.valueOf(rs.getString("type")), UUID.fromString(id), interactionData);
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
                          TABLE_NODE_SOURCE_IDS).stripIndent())) {
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
