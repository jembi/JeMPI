package org.jembi.jempi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.*;

final class PsqlQueries {
   private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);
   private static final String URL = String.format("jdbc:postgresql://postgresql:5432/%s", AppConfig.POSTGRESQL_DATABASE);

   private PsqlQueries() {
   }

   static User getUserByEmail(final String email) {
      try (Connection conn = DriverManager.getConnection(URL, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
           Statement stmt = conn.createStatement()) {
         ResultSet rs = stmt.executeQuery("select * from users where email = '" + email + "'");
         if (rs.next()) {
            return new User(
                  rs.getString("id"),
                  rs.getString("username"),
                  rs.getString("email"),
                  rs.getString("family_name"),
                  rs.getString("given_name")
            );
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return null;
   }

   static User registerUser(final User user) {
      String sql = "INSERT INTO users (given_name, family_name, email, username) VALUES"
                   + "('" + user.getGivenName() + "', '" + user.getFamilyName() + "', '" + user.getEmail() + "', '" + user.getUsername() + "')";
      try (Connection conn = DriverManager.getConnection(URL, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
           Statement statement = conn.createStatement()) {
         statement.executeUpdate(sql);
         LOGGER.info("Registered a new user");
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return getUserByEmail(user.getEmail());
   }

}
