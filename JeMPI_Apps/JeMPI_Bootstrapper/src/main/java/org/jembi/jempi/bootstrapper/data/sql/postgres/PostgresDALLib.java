package org.jembi.jempi.bootstrapper.data.sql.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

@FunctionalInterface
interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
public class PostgresDALLib {
    private final String url;
    private final String usr;
    private final String psw;
    public PostgresDALLib(String ip, int port, String db, String usr, String psw) {
        this.url = String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", ip, port, db);
        this.usr = usr;
        this.psw = psw;
    }

    private Connection GetConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.usr, this.psw);
    }

    public  <T extends PreparedStatement> Boolean RunQuery(ThrowingFunction<Connection, T, SQLException> getStatement) throws SQLException {
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
}


