package org.jembi.jempi.postgres;

import org.jembi.jempi.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnect {
   private static final String URL = "jdbc:postgresql://" + AppConfig.POSTGRES_SERVER + "/notifications";
   private static final String USER = "postgres";
   private static Connection conn;

   private DbConnect() {
   }

   public static Connection connect() {
      Connection conn = null;
      try {
         conn = DriverManager.getConnection(URL, USER, null);
      } catch (SQLException e) {
         System.out.println(e.getMessage());
      }

      return conn;
   }
}
