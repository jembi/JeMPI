package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
         Date res = new Date(created);
         String state = "New";

         String sql = "INSERT INTO notification (id, type, state, names, created_At, patient_id, golden_id, score) "
                      + "VALUES ('" + id + "','" + type + "','" + state + "','" + patientNames + "', '" + res + "', '" + dID
                      + "', '" + gID + "', '" + score + "')";
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
