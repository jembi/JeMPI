package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class DbConnect {
   private static final Logger LOGGER = LogManager.getLogger(DbConnect.class);

   private DbConnect() {
   }

   static Connection connect(
         final String URL,
         final String USR,
         final String PSW) {
      try {
         return DriverManager.getConnection(URL, USR, PSW);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return null;
   }

}
