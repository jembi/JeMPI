package org.jembi.jempi.postgres;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.lang.String;
import java.util.UUID;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.models.User;


public class PsqlQueries {

    private static final String QUERY = " select N.patient_id, N.id, N.names, N.created, NS.state," +
            " NT.type, M.score, M.golden_id from notification N " +
            "JOIN notification_state NS  ON NS.id = N.state_id " +
            "JOIN notification_type NT on N.type_id = NT.id " +
            "JOIN match M ON M.notification_id = N.id";
    private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);

    public static List getMatchesForReview() {

        ArrayList list = new ArrayList();

        try (Connection connection = dbConnect.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
            ResultSet rs = preparedStatement.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            UUID notification_id = null;
            HashMap row = new HashMap(columns);

            while (rs.next()) {
                row = new HashMap(columns);
                for (int i = 1; i <= columns; i++) {
                    if (md.getColumnName(i).equals("id")) {
                        notification_id = rs.getObject(i, java.util.UUID.class);
                    }
                    row.put(md.getColumnName(i), (rs.getObject(i)));
                }

                list.add(row);
                row.put("candidates", getCandidates(notification_id));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return list;
    }

    public static List getCandidates(UUID nID) {

        ArrayList list = new ArrayList();
        String candidates = "select notification_id, score, golden_id from candidates where notification_id IN ('" + nID + "')";
        try (Connection connection = dbConnect.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(candidates);) {
            ResultSet rs = preparedStatement.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                HashMap row = new HashMap(columns);
                for (int i = 1; i <= columns; i++) {
                    if (md.getColumnName(i).equals("notification_id")) {
                        row.put("score", (rs.getObject("score")));
                        row.put("golden_id", (rs.getObject("golden_id")));
                    }

                }
                if (!row.isEmpty())
                    list.add(row);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return list;
    }


    public static void insert(UUID id, String type, String patientNames, Float score, Long created, String gID, String dID) throws SQLException {

        Connection conn = dbConnect.connect();
        Statement stmt = conn.createStatement();

        // Set auto-commit to false
        conn.setAutoCommit(false);
        UUID stateId = null;
        UUID someType = null;
        Date res = new Date(created);

        ResultSet rs = stmt.executeQuery("select * from notification_state");
        while (rs.next()) {
            if (rs.getString("state").equals("New"))
                stateId = UUID.fromString(rs.getString("id"));
        }

        rs = stmt.executeQuery("select * from notification_type");
        while (rs.next()) {
            if (rs.getString("type").equals(type))
                someType = rs.getObject("id", java.util.UUID.class);
        }
        String sql = "INSERT INTO notification (id, type_id, state_id, names, created, patient_id) " +
                "VALUES ('" + id + "','" + someType + "','" + stateId + "','" + patientNames + "', '" + res + "', '" + dID + "')";
        stmt.addBatch(sql);

        sql = "INSERT INTO match (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
        stmt.addBatch(sql);


        int[] count = stmt.executeBatch();
        conn.commit();


    }

    public static void insert_candidates(UUID id, Float score, String gID) throws SQLException {
        Connection conn = dbConnect.connect();
        Statement stmt = conn.createStatement();
        conn.setAutoCommit(false);
        String sql = "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
        stmt.addBatch(sql);


        int[] count = stmt.executeBatch();
        conn.commit();
    }

    public static void updateNotificationState(String id, String state) throws SQLException {

        Connection conn = dbConnect.connect();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("update notification set state_id = " +
                "(select id from notification_state where state = '" + state + "' )where id = '" + id + "'");
        conn.commit();
    }

    public static User getUserByEmail(String email) throws SQLException {
        try {
            Connection conn = dbConnect.connect();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("select * from users where email = '" + email + "'");
            // Check if empty then return null
            if (rs.next()) {
                User user = new User(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("family_name"),
                        rs.getString("given_name")
                );
                return user;
            }
        } catch(SQLException e) {
            LOGGER.error(e);
        }
        return null;
    }

    public static User registerUser(User user) throws SQLException {
        Connection conn = dbConnect.connect();
        String sql = "INSERT INTO users (given_name, family_name, email, username) VALUES" +
                "('" + user.getGivenName() + "', '" + user.getFamilyName() + "', '" + user.getEmail() + "', '" + user.getUsername() + "')";
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);
        LOGGER.info("Registered a new user");
        return getUserByEmail(user.getEmail());
    }
}
