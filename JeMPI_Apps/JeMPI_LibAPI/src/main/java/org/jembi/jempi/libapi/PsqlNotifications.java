package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.MatchesForReviewResult;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

final class PsqlNotifications {
   private static final String QUERY = """
                                       SELECT patient_id, id, names, created, state,type, score, golden_id
                                       FROM notification
                                       WHERE created <= ? AND state = ?
                                       ORDER BY created
                                       LIMIT ? OFFSET ?
                                       """;
   private static final Logger LOGGER = LogManager.getLogger(PsqlNotifications.class);
   private final PsqlClient psqlClient;

   PsqlNotifications(
         final String pgServer,
         final int pgPort,
         final String pgDatabase,
         final String pgUser,
         final String pgPassword) {
      psqlClient = new PsqlClient(pgServer, pgPort, pgDatabase, pgUser, pgPassword);
   }

   /**
    * Retrieves matches for review based on the provided parameters.
    *
    * @param limit  The maximum number of matches to retrieve.
    * @param offset The number of matches to skip from the beginning.
    * @param date   The date threshold for match creation.
    * @param state  The state of notification.
    * @return A {@link MatchesForReviewResult} object containing the matches and related information.
    */
   MatchesForReviewResult getMatchesForReview(
         final int limit,
         final int offset,
         final LocalDate date,
         final String state) {
      final var list = new ArrayList<HashMap<String, Object>>();
      MatchesForReviewResult result = new MatchesForReviewResult();
      int skippedRows = 0;
      psqlClient.connect();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(QUERY); PreparedStatement countStatement =
            psqlClient.prepareStatement(
            "SELECT COUNT(*) FROM notification")) {
         ResultSet countRs = countStatement.executeQuery();
         countRs.next();
         int totalCount = countRs.getInt(1);

         preparedStatement.setDate(1, java.sql.Date.valueOf(date));
         preparedStatement.setString(2, state);
         preparedStatement.setInt(3, limit);
         preparedStatement.setInt(4, offset);
         LOGGER.debug("{}", preparedStatement);
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
            skippedRows++;
         }
         result.setCount(list.size());
         result.setSkippedRecords(totalCount - list.size());
      } catch (SQLException e) {
         LOGGER.error(e);
      }
      result.setNotifications(list);
      return result;
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
               "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
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
         ResultSet rs = stmt.executeQuery(String.format(Locale.ROOT,
                                                        "update notification set state = '%s' where id = '%s'",
                                                        state,
                                                        id));
         psqlClient.commit();
      }
   }

}
