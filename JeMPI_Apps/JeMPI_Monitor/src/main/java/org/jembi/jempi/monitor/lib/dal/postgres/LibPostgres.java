package org.jembi.jempi.monitor.lib.dal.postgres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.monitor.RestHttpServer;
import org.jembi.jempi.monitor.lib.dal.IDAL;

import java.sql.*;

@FunctionalInterface
interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
public class LibPostgres implements IDAL {
    private static final Logger LOGGER = LogManager.getLogger(RestHttpServer.class);

    private final String url;
    private final String usr;
    private final String psw;
    public LibPostgres(String url, String usr, String psw) {
        LOGGER.info("{}", "LibPostgresql Constructor");
        this.url = url;
        this.usr = usr;
        this.psw = psw;
    }

    private Connection GetConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.usr, this.psw);
    }

    private <T extends PreparedStatement> Boolean RunQuery(ThrowingFunction<Connection, T, SQLException> getStatement) throws SQLException {
        try (Connection connection = this.GetConnection()){
            connection.setAutoCommit(false);

            try{
                T statement = getStatement.apply(connection);
                statement.executeUpdate();
                connection.commit();
                return true;
            }catch (SQLException e){
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public boolean deleteAllData() throws SQLException {

        return this.RunQuery(connection -> {
            String deleteQuery = "SET session_replication_role = replica;"
                    + "DO $$ "
                    + "DECLARE "
                    + "    table_name text; "
                    + "BEGIN "
                    + "    FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') "
                    + "    LOOP "
                    + "        EXECUTE 'DELETE FROM ' || table_name || ';'; "
                    + "        RAISE NOTICE 'Deleted all records from table: %', table_name; "
                    + "    END LOOP; "
                    + "END $$;"
                    + "SET session_replication_role = DEFAULT;";
            return connection.prepareStatement(deleteQuery);
        });
    }
    public boolean deleteTableData(String tableName) throws SQLException {
        return this.RunQuery(connection -> connection.prepareStatement(String.format("DELETE FROM %s;", tableName)));
    }
}
