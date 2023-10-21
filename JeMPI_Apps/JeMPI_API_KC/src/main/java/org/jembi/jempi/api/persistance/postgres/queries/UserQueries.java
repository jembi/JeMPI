package org.jembi.jempi.api.persistance.postgres.queries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.persistance.postgres.QueryRunner;
import org.jembi.jempi.api.user.User;

import java.sql.*;


public class UserQueries extends QueryRunner {

    private static final Logger LOGGER = LogManager.getLogger(UserQueries.class);
    public User getUserByEmail(final String email) {
        try{
            QueryRunner.Executor executor = getPreparedStatement("SELECT * FROM users where email = ?");
            executor.preparedStatement.setString(1, email);

            ResultSet rs = executor.run();
            if (rs.next()) {
                return new User(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("family_name"),
                        rs.getString("given_name")
                );
            }
        }
        catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return null;

    }
    public User registerUser(final User user) {

        try{
            QueryRunner.Executor executor = getPreparedStatement("INSERT INTO users (given_name, family_name, email, username) VALUES (?, ?, ?, ?)");
            executor.preparedStatement.setString(1, user.getGivenName());
            executor.preparedStatement.setString(2, user.getFamilyName());
            executor.preparedStatement.setString(3, user.getEmail());
            executor.preparedStatement.setString(4, user.getUsername());

            executor.run();
            LOGGER.info("Registered a new user");
            return getUserByEmail(user.getEmail());
        }
        catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
