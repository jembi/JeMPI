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
   private final PsqlClient psqlClient;

   PsqlNotifications(
         final String pgDatabase,
         final String pgUser,
         final String pgPassword) {
      psqlClient = new PsqlClient(pgDatabase, pgUser, pgPassword);
   }

   List<HashMap<String, Object>> getMatchesForReview(
         final int limit,
         final int offset,
         final LocalDate date) {
      final var list = new ArrayList<HashMap<String, Object>>();
      psqlClient.connect();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(QUERY)) {
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
            row.put("candidates", getCandidates(notificationID));
         }
      } catch (SQLException e) {
         LOGGER.error(e);
      }
      return list;
   }

   List<HashMap<String, Object>> getCandidates(final UUID nID) {
      final var list = new ArrayList<HashMap<String, Object>>();
      String candidates = "select notification_id, score, golden_id from candidates where notification_id IN ('" + nID + "')";

      psqlClient.connect();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(candidates)) {
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
      } catch (SQLException e) {
         LOGGER.error(e);
      }
      return list;
   }

   void insertCandidates(
         final UUID id,
         final Float score,
         final String gID) throws SQLException {
      psqlClient.connect();
      try (Statement stmt = psqlClient.createStatement()) {
         psqlClient.setAutoCommit(false);
         String sql =
               "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID
               + "')";
         stmt.addBatch(sql);


         stmt.executeBatch();
         psqlClient.commit();
      }
   }

   void updateNotificationState(
         final String id,
         final String state) throws SQLException {
      psqlClient.connect();
      try (Statement stmt = psqlClient.createStatement()) {
         ResultSet rs = stmt.executeQuery("update notification set state_id = "
                                          + "(select id from notification_state where state = '" + state + "' )where id = '" + id
                                          + "'");
         psqlClient.commit();
      }
   }

}
