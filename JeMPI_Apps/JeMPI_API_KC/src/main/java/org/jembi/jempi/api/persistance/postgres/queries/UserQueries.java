package org.jembi.jempi.api.persistance.postgres.queries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.persistance.postgres.QueryRunner;
import org.jembi.jempi.api.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;


public final class UserQueries extends QueryRunner {

   private static final Logger LOGGER = LogManager.getLogger(UserQueries.class);

   public User getUser(final String username) {
      return this.getUser("username", username);
   }

   public User getUser(
         final String field,
         final String value) {
      try {

         ResultSet rs = executeQuery(String.format("SELECT * FROM users where %s = ?", field), preparedStatement -> {
            preparedStatement.setString(1, value);
         });

         if (rs.next()) {
            return new User(rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("family_name"),
                            rs.getString("given_name"));
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

      return null;

   }

   public User registerUser(final User user) {

      try {
         executeUpdate("INSERT INTO users (given_name, family_name, email, username) VALUES (?, ?, ?, ?)", preparedStatement -> {
            String givenName = user.getGivenName();
            String familyName = user.getFamilyName();
            String email = user.getEmail();
            String username = user.getUsername();

            preparedStatement.setString(1,
                                        givenName == null
                                              ? ""
                                              : givenName);
            preparedStatement.setString(2,
                                        familyName == null
                                              ? ""
                                              : familyName);
            preparedStatement.setString(3,
                                        email == null
                                              ? ""
                                              : email);
            preparedStatement.setString(4,
                                        username == null
                                              ? ""
                                              : username);
         });

         LOGGER.info("Registered a new user");
         return getUser(user.getUsername());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

      return null;
   }
}
