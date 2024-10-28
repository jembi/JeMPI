package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Locale;

final class PsqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PsqlClient.class);

   private static final String POSTGRESQL_IP = "postgres-1";
   private static final int POSTGRESQL_PORT = 5432;
   private static final String POSTGRESQL_USER = "postgres";
   private static final String POSTGRESQL_PASSWORD = "instant101";
   private static final String POSTGRESQL_DB = "mpi_db";

   private Connection connection;

   PsqlClient() {
      connection = null;
   }

   boolean connect() {
      if (connection == null) {
         try {
            final var url = String.format(Locale.ROOT,
                                          "jdbc:postgresql://%s:%d/%s",
                                          POSTGRESQL_IP,
                                          POSTGRESQL_PORT,
                                          POSTGRESQL_DB);
            connection = DriverManager.getConnection(url, POSTGRESQL_USER, POSTGRESQL_PASSWORD);
            connection.setAutoCommit(true);
            return connection.isValid(5);
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
            return false;
         }
      } else {
         try {
            if (!connection.isValid(5)) {
               connection.close();
               final var url = String.format(Locale.ROOT,
                                             "jdbc:postgresql://%s:%d/%s",
                                             POSTGRESQL_IP,
                                             POSTGRESQL_PORT,
                                             POSTGRESQL_DB);
               connection = DriverManager.getConnection(url, POSTGRESQL_USER, POSTGRESQL_PASSWORD);
            }
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
         }
      }
      try {
         if (connection == null) {
            return false;
         } else {
            return connection.isValid(5);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         connection = null;
         return false;
      }
   }

   void setAutoCommit(final boolean autoCommit) {
      try {
         connection.setAutoCommit(autoCommit);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void rollback() {
      try {
         connection.rollback();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void commit() {
      try {
         connection.commit();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void disconnect() {
      if (connection != null) {
         try {
            connection.close();
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
         connection = null;
      }
   }

   Statement createStatement() throws SQLException {
      return connection.createStatement();
   }

   PreparedStatement prepareStatement(final String sql) throws SQLException {
      return connection.prepareStatement(sql);
   }

   PreparedStatement prepareStatement(
         final String sql,
         final int resultSetType) throws SQLException {
      return connection.prepareStatement(sql, resultSetType);
   }

}
