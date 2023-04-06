package org.jembi.jempi.postgres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.models.User;

import java.sql.*;
import java.util.Date;
import java.util.*;


public final class PsqlQueries {
   private static final String QUERY = """
                                       select N.patient_id, N.id, N.names, N.created, NS.state,
                                              NT.type, M.score, M.golden_id from notification N
                                       JOIN notification_state NS ON NS.id = N.state_id
                                       JOIN notification_type NT ON N.type_id = NT.id
                                       JOIN match M ON M.notification_id = N.id
                                       """;
   private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);

   private PsqlQueries() {
   }

   public static List<HashMap<String, Object>> getMatchesForReview() {
      final var list = new ArrayList<HashMap<String, Object>>();
      try (Connection connection = DbConnect.connect();
           PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {
         ResultSet rs = preparedStatement.executeQuery();
         ResultSetMetaData md = rs.getMetaData();
         int columns = md.getColumnCount();
         UUID notificationID = null;
         while (rs.next()) {
            final var row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; i++) {
               if (md.getColumnName(i).equals("id")) {
                  notificationID = rs.getObject(i, java.util.UUID.class);
               }
               row.put(md.getColumnName(i), (rs.getObject(i)));
            }
            list.add(row);
            row.put("candidates", getCandidates(notificationID));
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   public static List<HashMap<String, Object>> getCandidates(final UUID nID) {
      final var list = new ArrayList<HashMap<String, Object>>();
      String candidates = "select notification_id, score, golden_id from candidates where notification_id IN ('" + nID + "')";
      try (Connection connection = DbConnect.connect();
           PreparedStatement preparedStatement = connection.prepareStatement(candidates)) {
         ResultSet rs = preparedStatement.executeQuery();
         ResultSetMetaData md = rs.getMetaData();
         int columns = md.getColumnCount();
         while (rs.next()) {
            final var row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; i++) {
               if (md.getColumnName(i).equals("notification_id")) {
                  row.put("score", (rs.getObject("score")));
                  row.put("golden_id", (rs.getObject("golden_id")));
               }
            }
            if (!row.isEmpty()) {
               list.add(row);
            }
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   public static void insert(
         final UUID id,
         final String type,
         final String patientNames,
         final Float score,
         final Long created,
         final String gID,
         final String dID) throws SQLException {

      Connection conn = DbConnect.connect();
      Statement stmt = conn.createStatement();

      // Set auto-commit to false
      conn.setAutoCommit(false);
      UUID stateId = null;
      UUID someType = null;
      Date res = new Date(created);

      ResultSet rs = stmt.executeQuery("select * from notification_state");
      while (rs.next()) {
         if (rs.getString("state").equals("New")) {
            stateId = UUID.fromString(rs.getString("id"));
         }
      }

      rs = stmt.executeQuery("select * from notification_type");
      while (rs.next()) {
         if (rs.getString("type").equals(type)) {
            someType = rs.getObject("id", java.util.UUID.class);
         }
      }
      String sql = "INSERT INTO notification (id, type_id, state_id, names, created, patient_id) "
                   + "VALUES ('" + id + "','" + someType + "','" + stateId + "','" + patientNames + "', '" + res + "', '" + dID + "')";
      stmt.addBatch(sql);

      sql = "INSERT INTO match (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
      stmt.addBatch(sql);

      int[] count = stmt.executeBatch();
      conn.commit();
      conn.close();
   }

   public static void insertCandidates(
         final UUID id,
         final Float score,
         final String gID) throws SQLException {
      Connection conn = DbConnect.connect();
      Statement stmt = conn.createStatement();
      conn.setAutoCommit(false);
      String sql =
            "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID
            + "')";
      stmt.addBatch(sql);


      int[] count = stmt.executeBatch();
      conn.commit();
      conn.close();
   }

   public static void updateNotificationState(
         final String id,
         final String state) throws SQLException {

      Connection conn = DbConnect.connect();
      Statement stmt = conn.createStatement();

      ResultSet rs = stmt.executeQuery("update notification set state_id = "
                                       + "(select id from notification_state where state = '" + state + "' )where id = '" + id
                                       + "'");
      conn.commit();
      conn.close();
   }

   public static User getUserByEmail(final String email) throws SQLException {
      try {
         Connection conn = DbConnect.connect();
         Statement stmt = conn.createStatement();

         ResultSet rs = stmt.executeQuery("select * from users where email = '" + email + "'");
         // Check if empty then return null
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
         LOGGER.error(e);
      }
      return null;
   }

   public static User registerUser(final User user) throws SQLException {
      Connection conn = DbConnect.connect();
      String sql = "INSERT INTO users (given_name, family_name, email, username) VALUES"
                   + "('" + user.getGivenName() + "', '" + user.getFamilyName() + "', '" + user.getEmail() + "', '" + user.getUsername() + "')";
      Statement statement = conn.createStatement();
      statement.executeUpdate(sql);
      LOGGER.info("Registered a new user");
      return getUserByEmail(user.getEmail());
   }
}
