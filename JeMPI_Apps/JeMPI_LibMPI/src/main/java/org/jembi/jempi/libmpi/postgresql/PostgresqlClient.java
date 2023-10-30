package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

final class PostgresqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PostgresqlClient.class);
   private Connection connection;

   private String url;
   private String usr;
   private String psw;

   private PostgresqlClient() {
      connection = null;
   }

   static PostgresqlClient getInstance() {
      return ClientHolder.INSTANCE;
   }

   void config(
         final String url,
         final String usr,
         final String psw) {
      this.url = url;
      this.usr = usr;
      this.psw = psw;
   }

   void startTransaction() {
      if (connection == null) {
         try {
            connection = DriverManager.getConnection(url, usr, psw);
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
         }
      }
   }

   void closeTransaction() {
   }

   void zapTransaction() {
      try {
         if (connection != null) {
            connection.close();
            connection = null;
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
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


   private static class ClientHolder {
      public static final PostgresqlClient INSTANCE = new PostgresqlClient();
   }


}
