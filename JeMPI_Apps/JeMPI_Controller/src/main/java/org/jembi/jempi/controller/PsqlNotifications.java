package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.*;

final class PsqlNotifications {
   private static final Logger LOGGER = LogManager.getLogger(PsqlNotifications.class);
   private static final String QUERY = "SELECT N.patient_id, N.id, N.names, N.created, NS.state, "
                                       + "NT.type, M.score, M.golden_id \n"
                                       + "FROM notification N \n"
                                       + "JOIN notification_state NS ON NS.id = N.state_id \n"
                                       + "JOIN notification_type NT ON N.type_id = NT.id \n"
                                       + "JOIN match M ON M.notification_id = N.id \n"
                                       + "WHERE N.created >= ? \n"
                                       + "ORDER BY N.id \n"
                                       + "LIMIT ? OFFSET ?";
   private final String pgDatabase;
   private final String pgUser;
   private final String pgPassword;
   private final PsqlClient psqlClient;
   private final String queryTwo = """
                                   SELECT N.patient_id, N.id, N.names, N.created, NS.state, NT.type, M.score, M.golden_id
                                   FROM notification N
                                   JOIN notification_state NS ON NS.id = N.state_id
                                   JOIN notification_type NT ON N.type_id = NT.id
                                   JOIN match M ON M.notification_id = N.id
                                   WHERE N.created >= ?
                                   ORDER BY N.id
                                   LIMIT ?, ?""";

   PsqlNotifications(
         final String database,
         final String user,
         final String password) {
      pgDatabase = database;
      pgUser = user;
      pgPassword = password;
      psqlClient = new PsqlClient();
   }

//   List<HashMap<String, Object>> getMatchesForReview(final String pgPassword) {
//      final var list = new ArrayList<HashMap<String, Object>>();
//      LocalDate date = LocalDate.of(2023, 6, 4);
//      int offset = 0;
//      int limit = 3;
//      try (Connection connection = DriverManager.getConnection(url, USER, pgPassword);
//           PreparedStatement preparedStatement = connection.prepareStatement(queryTwo)) {
//         preparedStatement.setDate(1, java.sql.Date.valueOf(date));
//         preparedStatement.setInt(2, offset);
//         preparedStatement.setInt(3, limit);
//
//         ResultSet rs = preparedStatement.executeQuery();
//         ResultSetMetaData md = rs.getMetaData();
//         int columns = md.getColumnCount();
//         UUID notificationID = null;
//         while (rs.next()) {
//            final var row = new HashMap<String, Object>(columns);
//            for (int i = 1; i <= columns; i++) {
//               if (md.getColumnName(i).equals("id")) {
//                  notificationID = rs.getObject(i, UUID.class);
//               }
//               row.put(md.getColumnName(i), rs.getObject(i));
//            }
//            list.add(row);
//            row.put("candidates", getCandidates(pgPassword, notificationID));
//         }
//      } catch (Exception e) {
//         LOGGER.error(e);
//      }
//      return list;
//   }

   List<HashMap<String, Object>> getMatchesForReview(
         final int limit,
         final int offset,
         final LocalDate date) {
      psqlClient.connect(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
      final var list = new ArrayList<HashMap<String, Object>>();
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
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   List<HashMap<String, Object>> getCandidates(final UUID nID) {
      final var list = new ArrayList<HashMap<String, Object>>();
      String candidates = "select notification_id, score, golden_id from candidates where notification_id IN ('" + nID + "')";
      psqlClient.connect(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
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
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   void insert(
         final UUID id,
         final String type,
         final String patientNames,
         final Float score,
         final Long created,
         final String gID,
         final String dID) throws SQLException {

      psqlClient.connect(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
      try (Statement stmt = psqlClient.createStatement()) {

         // Set auto-commit to false
         psqlClient.setAutoCommit(false);
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
               someType = rs.getObject("id", UUID.class);
            }
         }
         String sql = "INSERT INTO notification (id, type_id, state_id, names, created, patient_id) "
                      + "VALUES ('" + id + "','" + someType + "','" + stateId + "','" + patientNames + "', '" + res + "', '" + dID + "')";
         stmt.addBatch(sql);

         sql = "INSERT INTO match (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
         stmt.addBatch(sql);

         stmt.executeBatch();
         psqlClient.commit();
      }
   }

   void insertCandidates(
         final UUID id,
         final Float score,
         final String gID) throws SQLException {
      psqlClient.connect(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
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

      psqlClient.connect(AppConfig.POSTGRESQL_DATABASE, AppConfig.POSTGRESQL_USER, AppConfig.POSTGRESQL_PASSWORD);
      try (Statement stmt = psqlClient.createStatement()) {
         ResultSet rs = stmt.executeQuery("update notification set state_id = "
                                          + "(select id from notification_state where state = '" + state + "' )where id = '" + id
                                          + "'");
         psqlClient.commit();
      }
   }

}
