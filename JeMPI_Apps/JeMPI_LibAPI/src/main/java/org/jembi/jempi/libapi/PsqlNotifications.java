package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.MatchesForReviewResult;

import java.sql.*;
import java.util.*;

final class PsqlNotifications {

   private static final String NOTIFICATION_TABLE_NAME = "notification";
   private static final String QUERY = """
                                       SELECT patient_id, id, names, created, state,type, score, golden_id
                                       FROM notification
                                       WHERE created BETWEEN ? AND ? AND state IN (?, ?)
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
    * @param states The state of notification.
    * @return A {@link MatchesForReviewResult} object containing the matches and related information.
    */
   MatchesForReviewResult getMatchesForReview(
         final int limit,
         final int offset,
         final Timestamp startDate,
         final Timestamp endDate,
         final List<String> states) {
      final var list = new ArrayList<HashMap<String, Object>>();
      MatchesForReviewResult result = new MatchesForReviewResult();
      int skippedRows = 0;
      psqlClient.connect();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(QUERY);
           PreparedStatement countStatement = psqlClient.prepareStatement(
                 "SELECT COUNT(*) FROM notification WHERE created BETWEEN ? AND ? AND state IN (?, ?)")) {
         countStatement.setTimestamp(1, startDate);
         countStatement.setTimestamp(2, endDate);
         countStatement.setString(3, extractState(0, states));
         countStatement.setString(4, extractState(1, states));
         ResultSet countRs = countStatement.executeQuery();
         countRs.next();
         int totalCount = countRs.getInt(1);
         preparedStatement.setTimestamp(1, startDate);
         preparedStatement.setTimestamp(2, endDate);
         preparedStatement.setString(3, extractState(0, states));
         preparedStatement.setString(4, extractState(1, states));
         preparedStatement.setInt(5, limit);
         preparedStatement.setInt(6, offset);
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
               final var name = md.getColumnName(i);
               final var obj = rs.getObject(i);
               if (obj == null && "names".equals(name)) {
                  row.put(name, "");
               } else {
                  row.put(name, (obj));
               }
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

   public int getNotificationCount(final String status) {
      String queryStatement = status == null
            ? String.format("SELECT COUNT(*) FROM %s", NOTIFICATION_TABLE_NAME)
            : String.format("SELECT COUNT(*) FROM %s WHERE state = '%s'", NOTIFICATION_TABLE_NAME, status);

      psqlClient.connect();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(queryStatement);
           ResultSet resultSet = preparedStatement.executeQuery()) {
         if (resultSet.next()) {
            return resultSet.getInt(1);
         }
         return 0;
      } catch (SQLException e) {
         LOGGER.error(e);
      }
      return -1;
   }

   String extractState(
         final int index,
         final List<String> states) {
      if (index + 1 > states.size()) {
         return null;
      }
      return states.get(index);
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
         final String id) throws SQLException {
      psqlClient.connect();
      try (Statement stmt = psqlClient.createStatement()) {
         ResultSet rs = stmt.executeQuery(String.format(Locale.ROOT,
                                                        "update notification set state = '%s' where id = '%s'",
                                                        "CLOSED",
                                                        id));
         psqlClient.commit();
      }
   }

}
