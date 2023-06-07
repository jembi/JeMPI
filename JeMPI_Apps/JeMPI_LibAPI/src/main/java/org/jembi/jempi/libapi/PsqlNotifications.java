package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

final class PsqlNotifications {
   private static final String QUERY = """
                                       SELECT patient_id, id, names, created, state,type, score, golden_id
                                       FROM notification
                                       WHERE created >= ?
                                       ORDER BY created
                                       LIMIT ? OFFSET ?
                                       """;
   private static final Logger LOGGER = LogManager.getLogger(PsqlNotifications.class);
   private static final String USER = "postgres";
   private final String url;

   PsqlNotifications(final String db) {
      url = String.format("jdbc:postgresql://postgresql:5432/%s", db);
   }


   List<HashMap<String, Object>> getMatchesForReview(
         final String pgPassword,
         final int limit,
         final int offset,
         final LocalDate date) {
      final var list = new ArrayList<HashMap<String, Object>>();
      try (Connection connection = DriverManager.getConnection(url, USER, pgPassword);
           PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {
         preparedStatement.setDate(1, java.sql.Date.valueOf(date));
         preparedStatement.setInt(2, limit);
         preparedStatement.setInt(3, offset);
         ResultSet rs = preparedStatement.executeQuery();
         ResultSetMetaData md = rs.getMetaData();
         int columns = md.getColumnCount();
         UUID notificationID = null;
         while (rs.next()) {
            final var row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; i++) {
               if (md.getColumnName(i).equals("id")) {
                  notificationID = rs.getObject(i, UUID.class);
               }
               row.put(md.getColumnName(i), (rs.getObject(i)));
            }
            list.add(row);
            row.put("candidates", getCandidates(pgPassword, notificationID));
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   List<HashMap<String, Object>> getCandidates(
         final String pgPassword,
         final UUID nID) {
      final var list = new ArrayList<HashMap<String, Object>>();
      String candidates = "select notification_id, score, golden_id from candidates where notification_id IN ('" + nID + "')";

      try (Connection connection = DriverManager.getConnection(url, USER, pgPassword);
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

   void insertCandidates(
         final String pgPassword,
         final UUID id,
         final Float score,
         final String gID) throws SQLException {
      try (Connection conn = DriverManager.getConnection(url, USER, pgPassword);
           Statement stmt = conn.createStatement()) {
         conn.setAutoCommit(false);
         String sql =
               "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID
               + "')";
         stmt.addBatch(sql);


         stmt.executeBatch();
         conn.commit();
      }
   }

   void updateNotificationState(
         final String pgPassword,
         final String id,
         final String state) throws SQLException {

      try (
            Connection conn = DriverManager.getConnection(url, USER, pgPassword);
            Statement stmt = conn.createStatement()) {

         ResultSet rs = stmt.executeQuery("update notification set state_id = "
                                          + "(select id from notification_state where state = '" + state + "' )where id = '" + id
                                          + "'");
         conn.commit();
      }
   }

}
