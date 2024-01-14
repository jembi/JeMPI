package org.jembi.jempi.bootstrapper.data.sql.postgres;

import org.jembi.jempi.bootstrapper.BootstrapperConfig;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.data.utils.DataBootstraperConsts;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PostgresDataBootstrapper extends DataBootstrapper {

   private record DBSchemaDetails(String dbName, String schemaFilePath) { }
   private final PostgresDALLib postgresDALLib;

   public PostgresDataBootstrapper(final String configFilePath) {
      super(configFilePath);
      postgresDALLib = new PostgresDALLib(this.loadedConfig.POSTGRESQL_IP,
                                          this.loadedConfig.POSTGRESQL_PORT,
                                          this.loadedConfig.POSTGRESQL_USER,
                                          this.loadedConfig.POSTGRESQL_PASSWORD);
   }

   protected String getCreateSchemaScript(final String fileName) {
      InputStream postgresSchemaScript = this.getClass().getResourceAsStream(fileName);
      return new BufferedReader(new InputStreamReader(postgresSchemaScript, StandardCharsets.UTF_8)).lines()
                                                                                                    .collect(Collectors.joining(
                                                                                                          "\n"));
   }

   @Override
   public Boolean createSchema() throws SQLException {
      LOGGER.info("Loading Postgres schema data.");

      for (DBSchemaDetails schemaDetails: List.of(
              new DBSchemaDetails(this.loadedConfig.POSTGRESQL_USERS_DB, DataBootstraperConsts.POSTGRES_INIT_SCHEMA_USERS_DB),
              new DBSchemaDetails(this.loadedConfig.POSTGRESQL_NOTIFICATIONS_DB, DataBootstraperConsts.POSTGRES_INIT_SCHEMA_NOTIFICATION_DB),
              new DBSchemaDetails(this.loadedConfig.POSTGRESQL_AUDIT_DB, DataBootstraperConsts.POSTGRES_INIT_SCHEMA_AUDIT_DB),
              new DBSchemaDetails(this.loadedConfig.POSTGRESQL_KC_TEST_DB, null)
      )) {
         String dbName = schemaDetails.dbName();
         String dbSchemaFilePath = schemaDetails.schemaFilePath();

         LOGGER.info(String.format("---> Create schama for database %s", dbName));

         postgresDALLib.createDb(dbName);

         if (dbSchemaFilePath != null ) {
            postgresDALLib.runQuery(connection -> connection.prepareStatement(
                    String.format("""
                            USE %s
                            
                            %s
                            """, dbName, getCreateSchemaScript(dbSchemaFilePath))));
         }

      }
      return true;
   }

   protected String getAllTablesWrapper(final String innerQuery) {
      return String.format("""
                           SET session_replication_role = replica;
                           DO $$
                           DECLARE
                              table_name text;
                           BEGIN
                               FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public')
                               LOOP
                                   EXECUTE %s
                               END LOOP;
                           END $$;
                           SET session_replication_role = DEFAULT;
                           """, innerQuery);
   }

   public Boolean deleteTables() throws SQLException {
      LOGGER.info("Deleting Postgres tables");
      return postgresDALLib.runQuery(connection -> connection.prepareStatement(this.getAllTablesWrapper(
            "'DROP TABLE ' || table_name || ' CASCADE ;';")));
   }

   @Override
   public Boolean deleteData() throws SQLException {
      LOGGER.info("Deleting Postgres data");
      return postgresDALLib.runQuery(connection -> connection.prepareStatement(this.getAllTablesWrapper(
            "'DELETE FROM ' || table_name || ';';")));
   }

   @Override
   public Boolean resetAll() throws SQLException {
      LOGGER.info("Resetting Postgres data and schemas.");
      return this.deleteData() && this.deleteTables() && this.createSchema();
   }
}
