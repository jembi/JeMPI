package org.jembi.jempi.api.persistance.postgres;

import org.jembi.jempi.AppConfig;
import java.sql.*;

public  class QueryRunner {

    @FunctionalInterface
    public interface ExceptionalConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    @FunctionalInterface
    public interface ExceptionalFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }
    private static final String URL = String.format("jdbc:postgresql://%s:%d/%s",
            AppConfig.POSTGRESQL_IP,
            AppConfig.POSTGRESQL_PORT,
            AppConfig.POSTGRESQL_DATABASE);


    protected final Connection establishConnection() throws SQLException {
        return DriverManager.getConnection(URL, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
    }

    public <T> T executor(final String sqlQuery, ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater, ExceptionalFunction<PreparedStatement, T, SQLException> runner) throws SQLException {
        try (Connection connection =  establishConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            statementUpdater.accept(preparedStatement);
            return runner.apply(preparedStatement);

        }
    }
    public ResultSet executeQuery(final String sqlQuery, ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater) throws SQLException {
        return executor(sqlQuery, statementUpdater, PreparedStatement::executeQuery);
    }

    public int executeUpdate(final String sqlQuery, ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater) throws SQLException{
        return executor(sqlQuery, statementUpdater, PreparedStatement::executeUpdate);
    }
}
