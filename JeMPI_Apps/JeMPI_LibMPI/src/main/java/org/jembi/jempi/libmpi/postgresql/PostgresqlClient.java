package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public final class PostgresqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PostgresqlClient.class);
   private Connection connection;

   private String url;
   private String usr;
   private String psw;

   private PostgresqlClient() {
      connection = null;
   }

   public static PostgresqlClient getInstance() {
      return ClientHolder.INSTANCE;
   }

   public void config(
         final String url,
         final String usr,
         final String psw) {
      this.url = url;
      this.usr = usr;
      this.psw = psw;
   }

   public void startTransaction() {
      if (connection == null) {
         try {
            connection = DriverManager.getConnection(url, usr, psw);
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
         }
      }
   }

   public void closeTransaction() {
   }

   public void zapTransaction() {
      try {
         if (connection != null) {
            connection.close();
            connection = null;
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   public Statement createStatement() throws SQLException {
      return connection.createStatement();
   }

   public PreparedStatement prepareStatement(final String sql) throws SQLException {
      return connection.prepareStatement(sql);
   }

   public PreparedStatement prepareStatement(
         final String sql,
         final int resultSetType) throws SQLException {
      return connection.prepareStatement(sql, resultSetType);
   }


   private static class ClientHolder {
      public static final PostgresqlClient INSTANCE = new PostgresqlClient();
   }


}
