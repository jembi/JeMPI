package org.jembi.jempi.api.persistance.postgres;

import org.jembi.jempi.AppConfig;

import java.sql.*;

public class QueryRunner {

   private static final String URL = String.format("jdbc:postgresql://%s:%d/%s",
                                                   AppConfig.POSTGRESQL_IP,
                                                   AppConfig.POSTGRESQL_PORT,
                                                   AppConfig.POSTGRESQL_AUDIT_DB);

   protected final Connection establishConnection() throws SQLException {
      return DriverManager.getConnection(URL, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
   }

   /**
    * @param sqlQuery
    * @param statementUpdater
    * @param runner
    * @param <T>
    * @return
    * @throws SQLException
    */
   public <T> T executor(
         final String sqlQuery,
         final ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater,
         final ExceptionalFunction<PreparedStatement, T, SQLException> runner) throws SQLException {
      try (Connection connection = establishConnection()) {
         PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
         statementUpdater.accept(preparedStatement);
         return runner.apply(preparedStatement);

      }
   }

   /**
    * @param sqlQuery
    * @param statementUpdater
    * @return
    * @throws SQLException
    */
   public ResultSet executeQuery(
         final String sqlQuery,
         final ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater) throws SQLException {
      return executor(sqlQuery, statementUpdater, PreparedStatement::executeQuery);
   }

   /**
    * @param sqlQuery
    * @param statementUpdater
    * @return
    * @throws SQLException
    */
   public int executeUpdate(
         final String sqlQuery,
         final ExceptionalConsumer<PreparedStatement, SQLException> statementUpdater) throws SQLException {
      return executor(sqlQuery, statementUpdater, PreparedStatement::executeUpdate);
   }

   @FunctionalInterface
   public interface ExceptionalConsumer<T, E extends Exception> {
      void accept(T t) throws E;
   }

   @FunctionalInterface
   public interface ExceptionalFunction<T, R, E extends Exception> {
      R apply(T t) throws E;
   }
}
