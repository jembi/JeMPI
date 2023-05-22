package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Types;

final class PostgresqlMutations {

   static final String TABLE_EDGES = "mpi_edges";
   static final String TABLE_EDGES_GID2EID = "mpi_edges_gid2eid";
   static final String TABLE_EDGES_GID2SID = "mpi_edges_gid2sid";
   static final String TABLE_EDGES_EID2SID = "mpi_edges_eid2sid";
   static final String TABLE_NODES = "mpi_nodes";
   static final String TABLE_NODE_GOLDEN_RECORDS = "mpi_node_golden_records";
   static final String TABLE_NODE_INTERACTIONS = "mpi_node_interactions";
   static final String TABLE_NODE_SOURCE_IDS = "mpi_node_source_ids";
   static final String TYPE_NODE_TYPE = "mpi_node_type";
   static final String TYPE_EDGE_NAME = "mpi_edge_name";

   private static final Logger LOGGER = LogManager.getLogger(PostgresqlMutations.class);

   private PostgresqlMutations() {
   }

   static boolean dropAll() {
      LOGGER.debug("Drop All");
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {

         stmt.executeUpdate(
               String.format("""
                             DROP TABLE IF EXISTS %s
                             """, TABLE_EDGES).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             DROP TABLE IF EXISTS %s
                             """, TABLE_NODES).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             DROP TYPE IF EXISTS %s
                             """, TYPE_NODE_TYPE).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             DROP TYPE IF EXISTS %s
                             """, TYPE_EDGE_NAME).stripIndent());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return false;
      }
      return true;
   }

   static boolean createSchema() {
      LOGGER.debug("Create Schema");
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         stmt.executeUpdate("DROP EXTENSION IF EXISTS pg_trgm;");
         stmt.executeUpdate("CREATE EXTENSION pg_trgm;");
         stmt.executeUpdate("CREATE EXTENSION fuzzystrmatch;");
         stmt.executeUpdate("CREATE EXTENSION btree_gist;");
         stmt.executeUpdate(String.format(
               """
               CREATE TYPE %s AS ENUM ('%s','%s','%s');
               """,
               TYPE_NODE_TYPE,
               Node.NodeType.GOLDEN_RECORD.name(),
               Node.NodeType.INTERACTION.name(),
               Node.NodeType.SOURCE_ID.name()).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TYPE %s AS ENUM ('%s','%s','%s');
               """,
               TYPE_EDGE_NAME,
               Edge.EdgeName.EID2SID.name(),
               Edge.EdgeName.GID2SID.name(),
               Edge.EdgeName.GID2EID.name()).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s (
                   type %s NOT NULL,
                   id UUID NOT NULL DEFAULT gen_random_uuid(),
                   fields JSONB NOT NULL,
                   CREATED_AT TIMESTAMPTZ NOT NULL DEFAULT now(),
                   UPDATED_AT TIMESTAMPTZ NOT NULL DEFAULT now(),
                   CONSTRAINT PKEY_NODES PRIMARY KEY (id, type)
               ) PARTITION BY LIST(type);
               """,
               TABLE_NODES,
               TYPE_NODE_TYPE).stripIndent());
         stmt.executeUpdate(String.format(
               """                  
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_NODE_GOLDEN_RECORDS,
               TABLE_NODES,
               Node.NodeType.GOLDEN_RECORD).stripIndent());
         stmt.executeUpdate(String.format(
               """                  
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_NODE_INTERACTIONS,
               TABLE_NODES,
               Node.NodeType.INTERACTION).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_NODE_SOURCE_IDS,
               TABLE_NODES,
               Node.NodeType.SOURCE_ID).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s (
                  name %s NOT NULL,
                  source UUID NOT NULL,
                  dest UUID NOT NULL,
                  facet JSONB,
                  CONSTRAINT PKEY_EDGES PRIMARY KEY (name, source, dest)
               ) PARTITION BY LIST(name);
               """,
               TABLE_EDGES,
               TYPE_EDGE_NAME).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_EDGES_GID2EID,
               TABLE_EDGES,
               Edge.EdgeName.GID2EID).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_EDGES_GID2SID,
               TABLE_EDGES,
               Edge.EdgeName.GID2SID).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s
               PARTITION OF %s
               FOR VALUES IN ('%s');
               """,
               TABLE_EDGES_EID2SID,
               TABLE_EDGES,
               Edge.EdgeName.EID2SID).stripIndent());

         stmt.executeUpdate(String.format(
               """
               CREATE INDEX IF NOT EXISTS idx_gin_gr_a ON %s USING gin (fields jsonb_ops);
               """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
         stmt.executeUpdate(String.format(
               """
               CREATE INDEX IF NOT EXISTS idx_gin_gr_b ON %s USING gin (fields jsonb_path_ops);
               """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
         CustomMutations.createSchema(stmt);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return false;
      }
      return true;
   }

   static boolean updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      final var sql = String.format(
            """
            UPDATE %s
            SET "fields" = JSONB_SET("fields"::JSONB, '{%s}', TO_JSONB('%s'::TEXT))
            WHERE id = ?;
            """,
            TABLE_NODE_GOLDEN_RECORDS,
            fieldName,
            val).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(sql)) {
         stmt.setObject(1, goldenId, Types.OTHER);
         final var rs = stmt.executeUpdate();
         return rs == 1;
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return false;
   }

   static boolean setScore(
         final String patientUID,
         final String goldenRecordUid,
         final float score) {
      final var sql = String.format(
            """
            UPDATE %s
            SET facet = JSONB_SET(facet, '{score}', to_jsonb(%f))
            WHERE source = '%s' AND dest = '%s';
            """,
            TABLE_EDGES_GID2EID,
            score,
            goldenRecordUid,
            patientUID).stripIndent();
      try (var stmt = PostgresqlClient.getInstance().createStatement()) {
         final var rs = stmt.executeUpdate(sql);
         return rs == 1;
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return false;
   }

}
