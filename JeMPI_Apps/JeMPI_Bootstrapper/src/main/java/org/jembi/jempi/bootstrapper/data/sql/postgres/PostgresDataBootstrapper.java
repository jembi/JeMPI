package org.jembi.jempi.bootstrapper.data.sql.postgres;

import org.jembi.jempi.bootstrapper.data.DataBootstrapper;

import java.sql.SQLException;

public class PostgresDataBootstrapper extends DataBootstrapper {
    private PostgresDALLib postgresDALLib;
    public PostgresDataBootstrapper(String configFilePath) {
        super(configFilePath);
        postgresDALLib = new PostgresDALLib(this.loadedConfig.POSTGRESQL_IP,
                this.loadedConfig.POSTGRESQL_PORT,
                this.loadedConfig.POSTGRESQL_DATABASE,
                this.loadedConfig.POSTGRESQL_USER,
                this.loadedConfig.POSTGRESQL_PASSWORD);
    }

    @Override
    public Boolean createSchema() throws SQLException {
        LOGGER.info("Loading Postgres schema data.");
        // TODO: <ove these to static files
        String createQuery = """
                        CREATE TABLE IF NOT EXISTS Notification_Type
                        (
                            Id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
                            Type VARCHAR(50)
                        );
                                
                        CREATE TABLE IF NOT EXISTS Action_Type
                        (
                            Id UUID DEFAULT gen_random_uuid() PRIMARY KEY UNIQUE,
                            Type VARCHAR(50)
                        );
                                
                        CREATE TABLE IF NOT EXISTS Notification_State
                        (
                            Id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                            State VARCHAR(50)
                        );
                                
                        CREATE TABLE IF NOT EXISTS Notification
                        (
                            Id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
                            Type VARCHAR(50),
                            Created date,
                            Reviewd_By uuid,
                            Reviewed_At timestamp without time zone,
                            State VARCHAR(50),
                            Patient_Id VARCHAR(50),
                            Names VARCHAR(100),
                            Golden_Id VARCHAR(50),
                            Score Numeric
                        );
                                
                        CREATE TABLE IF NOT EXISTS Action
                        (
                            Id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                            Notification_Id UUID,
                            Action_Type_Id UUID,
                            Date date,
                            CONSTRAINT FK_Notification
                              FOREIGN KEY(Notification_Id)\s
                        	    REFERENCES Notification(Id),
                            CONSTRAINT FK_Action_Type
                              FOREIGN KEY(Action_Type_Id)\s
                        	    REFERENCES Action_Type(Id)
                        );
                                
                        CREATE TABLE IF NOT EXISTS Match
                        (
                            Notification_Id UUID,
                            Score Numeric,
                            Golden_Id VARCHAR(50),
                            CONSTRAINT FK_Notification
                              FOREIGN KEY(Notification_Id)\s
                        	    REFERENCES Notification(Id)
                        );
                                
                        CREATE TABLE IF NOT EXISTS candidates
                        (
                            Notification_Id UUID,
                            Score Numeric,
                            Golden_Id VARCHAR(50),
                            CONSTRAINT FK_Notification
                              FOREIGN KEY(Notification_Id)\s
                        	    REFERENCES Notification(Id)
                        );
                                
                        CREATE TABLE IF NOT EXISTS users
                        (
                            id UUID DEFAULT gen_random_uuid() PRIMARY KEY UNIQUE,
                            given_name VARCHAR(255),
                            family_name VARCHAR(255),
                            email VARCHAR(255) UNIQUE,
                            username VARCHAR(255) UNIQUE
                        );
                                
                        INSERT INTO Notification_State(State)
                        VALUES ('New'), ('Seen'), ('Actioned'), ('Accepted'), ('Pending');
                                
                        INSERT INTO Notification_Type(Type)
                        VALUES ('THRESHOLD'), ('MARGIN'), ('UPDATE');
                """;

        return postgresDALLib.RunQuery(connection -> {
            return connection.prepareStatement(createQuery);
        });
    }

    public Boolean deleteTables() throws SQLException{
        LOGGER.info("Deleting Postgres tables");
        //TODO: Repetiton
        return postgresDALLib.RunQuery(connection -> {
            String deleteQuery = "SET session_replication_role = replica;"
                    + "DO $$ "
                    + "DECLARE "
                    + "    table_name text; "
                    + "BEGIN "
                    + "    FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') "
                    + "    LOOP "
                    + "        EXECUTE 'DROP TABLE ' || table_name || ' CASCADE ;'; "
                    + "    END LOOP; "
                    + "END $$;"
                    + "SET session_replication_role = DEFAULT;";
            return connection.prepareStatement(deleteQuery);
        });
    }
    @Override
    public Boolean deleteData() throws SQLException {
        LOGGER.info("Deleting Postgres data");
        return postgresDALLib.RunQuery(connection -> {
            String deleteQuery = "SET session_replication_role = replica;"
                    + "DO $$ "
                    + "DECLARE "
                    + "    table_name text; "
                    + "BEGIN "
                    + "    FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') "
                    + "    LOOP "
                    + "        EXECUTE 'DELETE FROM ' || table_name || ';'; "
                    + "    END LOOP; "
                    + "END $$;"
                    + "SET session_replication_role = DEFAULT;";
            return connection.prepareStatement(deleteQuery);
        });
    }

    @Override
    public Boolean resetAll() throws SQLException{
        LOGGER.info("Resetting Postgres data and schemas.");
        return this.deleteData() && this.deleteTables() && this.createSchema();
    }
}
