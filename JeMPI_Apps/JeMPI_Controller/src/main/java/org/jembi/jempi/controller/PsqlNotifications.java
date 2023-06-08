package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

final class PsqlNotifications {
   private static final Logger LOGGER = LogManager.getLogger(PsqlNotifications.class);
   private final PsqlClient psqlClient;

   PsqlNotifications() {
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

   void insert(
         final UUID id,
         final String type,
         final String patientNames,
         final Float score,
         final Long created,
         final String gID,
         final String dID) throws SQLException {

      psqlClient.connect();
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

}
