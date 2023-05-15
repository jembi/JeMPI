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
   static final String TABLE_NODES_GOLDEN_RECORD = "mpi_nodes_golden_record";
   static final String TABLE_NODES_ENCOUNTER = "mpi_nodes_encounter";
   static final String TABLE_NODES_SOURCE_ID = "mpi_nodes_source_id";
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
                             drop table if exists %s
                             """, TABLE_EDGES).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             drop table if exists %s
                             """, TABLE_NODES).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             drop type if exists %s
                             """, TYPE_NODE_TYPE).stripIndent());

         stmt.executeUpdate(
               String.format("""
                             drop type if exists %s
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
         stmt.executeUpdate(
               String.format("""
                             CREATE TYPE %s AS ENUM ('%s','%s','%s');
                             """,
                             TYPE_NODE_TYPE,
                             Node.NodeType.GOLDEN_RECORD.name(),
                             Node.NodeType.ENCOUNTER.name(),
                             Node.NodeType.SOURCE_ID.name()).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create type %s as enum ('%s','%s','%s');
                             """,
                             TYPE_EDGE_NAME,
                             Edge.EdgeName.EID2SID.name(),
                             Edge.EdgeName.GID2SID.name(),
                             Edge.EdgeName.GID2EID.name()).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s (
                                 type %s not null,
                                 id uuid not null default gen_random_uuid(),
                                 fields jsonb not null,
                                 created_at timestamptz not null default now(),
                                 updated_at timestamptz not null default now(),
                                 constraint pkey_nodes primary key (id, type)
                             ) partition by list(type);
                             """,
                             TABLE_NODES,
                             TYPE_NODE_TYPE).stripIndent());
         stmt.executeUpdate(
               String.format("""                  
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_NODES_GOLDEN_RECORD,
                             TABLE_NODES,
                             Node.NodeType.GOLDEN_RECORD).stripIndent());
         stmt.executeUpdate(
               String.format("""                  
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_NODES_ENCOUNTER,
                             TABLE_NODES,
                             Node.NodeType.ENCOUNTER).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_NODES_SOURCE_ID,
                             TABLE_NODES,
                             Node.NodeType.SOURCE_ID).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s (
                                name %s not null,
                                source uuid not null,
                                dest uuid not null,
                                facet jsonb,
                                constraint pkey_edges primary key (name, source, dest)
                             ) partition by list(name);
                             """,
                             TABLE_EDGES,
                             TYPE_EDGE_NAME).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_EDGES_GID2EID,
                             TABLE_EDGES,
                             Edge.EdgeName.GID2EID).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_EDGES_GID2SID,
                             TABLE_EDGES,
                             Edge.EdgeName.GID2SID).stripIndent());
         stmt.executeUpdate(
               String.format("""
                             create table if not exists %s
                             partition of %s
                             for values in ('%s');
                             """,
                             TABLE_EDGES_EID2SID,
                             TABLE_EDGES,
                             Edge.EdgeName.EID2SID).stripIndent());

         stmt.executeUpdate("""
                            create index if not exists idx_gin_gr_a on mpi_nodes_golden_record using gin (fields jsonb_ops);
                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gin_gr_b on mpi_nodes_golden_record using gin (fields jsonb_path_ops);
                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_gin_gr_c on mpi_nodes_golden_record using gin ((fields->>'givenName') gin_trgm_ops);
//                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gist_gr_a on mpi_nodes_golden_record using gist ((fields->>'givenName') gist_trgm_ops);
                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gist_gr_b on mpi_nodes_golden_record using gist ((fields->>'familyName') gist_trgm_ops);
                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gist_gr_c on mpi_nodes_golden_record using gist ((fields->>'phoneNumber') gist_trgm_ops);
                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gist_gr_d on mpi_nodes_golden_record using gist ((fields->>'city') gist_trgm_ops);
                            """.stripIndent());
         stmt.executeUpdate("""
                            create index if not exists idx_gist_gr_e on mpi_nodes_golden_record using gist ((fields->>'nationalId') gist_trgm_ops);
                            """.stripIndent());

//         "create index ind_b on mpi_nodes_golden_record using gin ((fields->>'givenname') gin_trgm_ops);"
//         "create index idx_e on mpi_nodes_golden_record using gist ((fields->>'givenname') gist_trgm_ops);"
//         "create index idx_c on mpi_nodes_golden_record ((fields#>>'{nationalid}'));"
//         "create index idx_d on mpi_nodes_golden_record ((fields->>'{givenname}'));"

//         stmt.executeUpdate("""
//                            create index if not exists idx_btree_gr_a on mpi_nodes_golden_record using btree (
//                            (fields->>'givenName'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_btree_gr_b on mpi_nodes_golden_record using btree (
//                            (fields->>'familyName'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_btree_gr_c on mpi_nodes_golden_record using btree (
//                            (fields->>'city'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_btree_gr_d on mpi_nodes_golden_record using btree (
//                            (fields->>'phoneNumber'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_btree_gr_e on mpi_nodes_golden_record using btree (
//                            (fields->>'nationalId'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_hash_gr_a on mpi_nodes_golden_record using hash (
//                            (fields->>'givenName'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_hash_gr_b on mpi_nodes_golden_record using hash (
//                            (fields->>'familyName'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_hash_gr_c on mpi_nodes_golden_record using hash ((fields->>'city'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_hash_gr_d on mpi_nodes_golden_record using hash (
//                            (fields->>'phoneNumber'));
//                            """.stripIndent());
//         stmt.executeUpdate("""
//                            create index if not exists idx_hash_gr_e on mpi_nodes_golden_record using hash (
//                            (fields->>'nationalId'));
//                            """.stripIndent());
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
      final var sql = String.format("""
                                    update %s
                                    set "fields" = jsonb_set("fields"::jsonb, '{%s}', to_jsonb('%s'::text))
                                    where id = ?;
                                    """,
                                    TABLE_NODES_GOLDEN_RECORD,
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
      final var sql = String.format("""
                                    update %s
                                    set facet = jsonb_set(facet, '{score}', to_jsonb(%f)) where source = '%s' and dest = '%s';
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
