package org.jembi.jempi.bootstrapper.data.sql.postgres;

import org.jembi.jempi.bootstrapper.data.DataBootstrapper;
import org.jembi.jempi.bootstrapper.data.utils.DataBootstraperConsts;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class PostgresDataBootstrapper extends DataBootstrapper {
    private final PostgresDALLib postgresDALLib;
    public PostgresDataBootstrapper(String configFilePath) {
        super(configFilePath);
        postgresDALLib = new PostgresDALLib(this.loadedConfig.POSTGRESQL_IP,
                this.loadedConfig.POSTGRESQL_PORT,
                this.loadedConfig.POSTGRESQL_DATABASE,
                this.loadedConfig.POSTGRESQL_USER,
                this.loadedConfig.POSTGRESQL_PASSWORD);
    }

    protected String getCreateSchemaScript(){
        InputStream postgresSchemaScript = this.getClass().getResourceAsStream(DataBootstraperConsts.POSTGRES_INIT_SCHEMA_SQL);
        return new BufferedReader(new InputStreamReader(postgresSchemaScript, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }
    @Override
    public Boolean createSchema() throws SQLException {
        LOGGER.info("Loading Postgres schema data.");
        return postgresDALLib.RunQuery(connection -> {
            return connection.prepareStatement(getCreateSchemaScript());
        });
    }

    protected String GetAllTablesWrapper(String innerQuery){
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
    public Boolean deleteTables() throws SQLException{
        LOGGER.info("Deleting Postgres tables");
        return postgresDALLib.RunQuery(connection ->
                connection.prepareStatement(this.GetAllTablesWrapper("'DROP TABLE ' || table_name || ' CASCADE ;';")));
    }
    @Override
    public Boolean deleteData() throws SQLException {
        LOGGER.info("Deleting Postgres data");
        return postgresDALLib.RunQuery(connection ->
                connection.prepareStatement(this.GetAllTablesWrapper("'DELETE FROM ' || table_name || ';';")));
    }

    @Override
    public Boolean resetAll() throws SQLException{
        LOGGER.info("Resetting Postgres data and schemas.");
        return this.deleteData() && this.deleteTables() && this.createSchema();
    }
}
