package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.AuditEvent;

import java.sql.SQLException;
import java.sql.Timestamp;

import static org.jembi.jempi.shared.models.GlobalConstants.PSQL_TABLE_AUDIT_TRAIL;

final class PsqlAuditTrail {
   private static final Logger LOGGER = LogManager.getLogger(PsqlAuditTrail.class);
   private final PsqlClient psqlClient;

   PsqlAuditTrail() {
      psqlClient = new PsqlClient();
   }

   void createSchemas() {
      LOGGER.debug("Create Schemas");
      psqlClient.connect();
      try (var stmt = psqlClient.createStatement()) {
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s (
                   id             UUID           NOT NULL DEFAULT gen_random_uuid(),
                   insertedAt     TIMESTAMPTZ    NOT NULL DEFAULT now(),
                   createdAt      TIMESTAMPTZ    NOT NULL,
                   interactionID  VARCHAR(64),
                   goldenID       VARCHAR(64),
                   event          VARCHAR(256),
                   CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
               );
               """,
               PSQL_TABLE_AUDIT_TRAIL).stripIndent());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void addAuditEvent(final AuditEvent event) {
      psqlClient.connect();
      try (var preparedStatement = psqlClient.prepareStatement(
            String.format(
                  """
                  INSERT INTO %s (createdAt, interactionID, goldenID, event)
                  VALUES (?, ?, ?, ?);
                  """, PSQL_TABLE_AUDIT_TRAIL).stripIndent())) {
         preparedStatement.setTimestamp(1, new Timestamp(event.createdAt()));
         preparedStatement.setString(2, event.interactionID());
         preparedStatement.setString(3, event.goldenID());
         preparedStatement.setString(4, event.event());
         preparedStatement.executeUpdate();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

}
