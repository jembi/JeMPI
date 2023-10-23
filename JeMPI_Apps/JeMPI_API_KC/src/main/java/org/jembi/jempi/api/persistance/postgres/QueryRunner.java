package org.jembi.jempi.api.persistance.postgres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.*;

public  class QueryRunner {
    private static final Logger LOGGER = LogManager.getLogger(QueryRunner.class);
    private static final String URL = String.format("jdbc:postgresql://%s:%d/%s",
            AppConfig.POSTGRESQL_IP,
            AppConfig.POSTGRESQL_PORT,
            AppConfig.POSTGRESQL_DATABASE);


    protected final Connection establishConnection() throws SQLException {
        return DriverManager.getConnection(URL, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
    }

    public final class Executor {
        public final Connection connection;
        public final PreparedStatement preparedStatement;

        public Executor(final Connection connection, final PreparedStatement preparedStatement) {
            this.connection = connection;
            this.preparedStatement = preparedStatement;
        }

        public ResultSet run() throws SQLException {
            return preparedStatement.executeQuery();
        }
    }
    public Executor getPreparedStatement(final String sqlQuery) throws SQLException {
        try (
            Connection connection = establishConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                return new Executor(connection, preparedStatement);
            }
    }
}
