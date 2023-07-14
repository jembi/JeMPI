package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.*;

final class PsqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PsqlClient.class);
   private Connection connection;

   PsqlClient() {
      connection = null;
   }

   boolean connect() {
      if (connection == null) {
         try {
            final var url = String.format("jdbc:postgresql://" + AppConfig.POSTGRESQL_SERVER + "/%s", AppConfig.POSTGRESQL_DATABASE);
            connection = DriverManager.getConnection(url, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
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
               final var url = String.format("jdbc:postgresql://" + AppConfig.POSTGRESQL_SERVER + "/%s", AppConfig.POSTGRESQL_DATABASE);
               connection = DriverManager.getConnection(url, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
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
