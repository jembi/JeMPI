package org.jembi.jempi.libmpi.postgresql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import static org.jembi.jempi.libmpi.postgresql.PostgresqlMutations.TABLE_NODE_GOLDEN_RECORDS;

final class CustomMutations {

   private CustomMutations() {
   }

   static void createSchema(final Statement stmt) throws SQLException {
      stmt.executeUpdate(String.format(
            Locale.ROOT,
            """
            CREATE INDEX IF NOT EXISTS idx_gist_gr_a ON %s USING GIST ((fields->>'givenName') GIST_TRGM_OPS);
            """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
      stmt.executeUpdate(String.format(
            Locale.ROOT,
            """
            CREATE INDEX IF NOT EXISTS idx_gist_gr_b ON %s USING GIST ((fields->>'familyName') GIST_TRGM_OPS);
            """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
      stmt.executeUpdate(String.format(
            Locale.ROOT,
            """
            CREATE INDEX IF NOT EXISTS idx_gist_gr_c ON %s USING GIST ((fields->>'phoneNumber') GIST_TRGM_OPS);
            """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
      stmt.executeUpdate(String.format(
            Locale.ROOT,
            """
            CREATE INDEX IF NOT EXISTS idx_gist_gr_d ON %s USING GIST ((fields->>'city') GIST_TRGM_OPS);
            """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());
      stmt.executeUpdate(String.format(
            Locale.ROOT,
            """
            CREATE INDEX IF NOT EXISTS idx_gist_gr_e ON %s USING GIST ((fields->>'nationalId') GIST_TRGM_OPS);
            """, TABLE_NODE_GOLDEN_RECORDS).stripIndent());

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

   }

}
