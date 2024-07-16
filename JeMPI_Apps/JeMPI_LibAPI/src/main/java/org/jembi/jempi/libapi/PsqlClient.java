package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Locale;

final class PsqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PsqlClient.class);
   private final String dbIP;
   private final int dbPort;
   private final String database;
   private final String user;
   private final String password;

   private Connection connection;

   PsqlClient(
         final String pgIP,
         final int pgPort,
         final String pgDatabase,
         final String pgUser,
         final String pgPassword) {
      dbIP = pgIP;
      dbPort = pgPort;
      connection = null;
      database = pgDatabase;
      user = pgUser;
      password = pgPassword;
   }

   boolean connect() {
      if (connection == null) {
         final var url = String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", dbIP, dbPort, database);
         try {
            connection = DriverManager.getConnection(url, user, password);
            return connection.isValid(5);
         } catch (SQLException e) {
            LOGGER.error("Connection error with URL: {}", url);
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
            return false;
         }
      } else {
         try {
            if (!connection.isValid(5)) {
               connection.close();
               final var url = String.format(Locale.ROOT, "jdbc:postgresql://%s:%d/%s", dbIP, dbPort, database);
               connection = DriverManager.getConnection(url, user, password);
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
