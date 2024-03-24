package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.UUID;

final class PsqlNotifications {
   private static final Logger LOGGER = LogManager.getLogger(PsqlNotifications.class);
   private final PsqlClient psqlClient;

   PsqlNotifications() {
      psqlClient = new PsqlClient();
   }

   void insert(
         final UUID id,
         final String type,
         final String patientNames,
         final Float score,
         final Timestamp created,
         final String gID,
         final String dID) throws SQLException {

      psqlClient.connect(AppConfig.POSTGRESQL_NOTIFICATIONS_DB);
      String sql = "INSERT INTO notification (id, type, state, names, created, patient_id, old_golden_id, current_golden_id, score) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
      try (PreparedStatement pstmt = psqlClient.prepareStatement(sql)) {
         psqlClient.setAutoCommit(false);
         pstmt.setObject(1, id);
         pstmt.setString(2, type);
         pstmt.setString(3, "OPEN");
         pstmt.setString(4, patientNames);
         pstmt.setTimestamp(5, created);
         pstmt.setString(6, dID);
         pstmt.setString(7, gID);
         pstmt.setString(8, gID);
         pstmt.setFloat(9, score);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         LOGGER.error("Error executing INSERT statement: {}", e.getMessage(), e);
      } finally {
         psqlClient.setAutoCommit(true);
      }
   }

   void insertCandidates(
         final UUID id,
         final Float score,
         final String gID) throws SQLException {
      psqlClient.connect(AppConfig.POSTGRESQL_NOTIFICATIONS_DB);
      try (Statement stmt = psqlClient.createStatement()) {
         psqlClient.setAutoCommit(false);
         String sql =
               "INSERT INTO candidates (notification_id, score, golden_id)" + " VALUES ('" + id + "','" + score + "', '" + gID + "')";
         stmt.addBatch(sql);
         stmt.executeBatch();
         psqlClient.commit();
      }
   }

}
