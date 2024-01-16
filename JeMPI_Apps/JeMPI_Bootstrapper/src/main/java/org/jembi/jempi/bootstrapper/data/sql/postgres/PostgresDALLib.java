package org.jembi.jempi.bootstrapper.data.sql.postgres;

import java.sql.*;
import java.util.Locale;


public class PostgresDALLib {

   private final String usr;
   private final String psw;
   private final String ip;
   private final String defaultDb;
   private final int port;
   public PostgresDALLib(
         final String ip,
         final int port,
         final String usr,
         final String db,
         final String psw) {
      this.ip = ip;
      this.port = port;
      this.defaultDb = db;
      this.usr = usr;
      this.psw = psw;
   }

   private String getDbUrl(final String db) {
      return  String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", ip, port, db);
   }
   private Connection getConnection(final String dbName) throws SQLException {
      return DriverManager.getConnection(getDbUrl(dbName != null ? dbName : defaultDb), this.usr, this.psw);
   }

   public Boolean createDb(final String dbName) throws SQLException {
      if (!databaseExists(dbName)) {
         return runQuery(connection -> {
            return connection.prepareStatement(getCreateDbSchema(dbName));
         }, true, null);
      }
      return true;
   }

   protected  boolean databaseExists(final String databaseName) throws SQLException {
      String query = "SELECT 1 FROM pg_database WHERE datname = ?";
      try (PreparedStatement preparedStatement = getConnection(null).prepareStatement(query)) {
         preparedStatement.setString(1, databaseName);
         try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next();
         }
      }
   }
   public String getCreateDbSchema(final String dbName) {
      return String.format("""
                           CREATE DATABASE %s
                           """, dbName);
   }

   public <T extends PreparedStatement> Boolean runQuery(final ThrowingFunction<Connection, T, SQLException> getStatement, final Boolean autoCommit, final String dbName) throws SQLException {
      try (Connection connection = this.getConnection(dbName)) {
         connection.setAutoCommit(autoCommit);

         try {
            T statement = getStatement.apply(connection);
            if (statement != null) {
               statement.executeUpdate();
               if (!autoCommit) {
                  connection.commit();
               }

            }

            return true;
         } catch (SQLException e) {
            if (!autoCommit) {
               connection.rollback();
            }

            throw e;
         }
      } catch (SQLException e) {
         throw e;
      }
   }

   @FunctionalInterface
   interface ThrowingFunction<T, R, E extends Exception> {
      R apply(T t) throws E;
   }
}


