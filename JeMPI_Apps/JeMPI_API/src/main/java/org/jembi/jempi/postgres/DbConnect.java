package org.jembi.jempi.postgres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class DbConnect {
   private static final Logger LOGGER = LogManager.getLogger(DbConnect.class);
   private static final String URL = "jdbc:postgresql://" + AppConfig.POSTGRES_SERVER + "/notifications";
   private static final String USER = "postgres";

   private DbConnect() {
   }

   static Connection connect() {
      try {
         LOGGER.debug("{}", URL);
         return DriverManager.getConnection(URL, USER, null);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return null;
   }

}
