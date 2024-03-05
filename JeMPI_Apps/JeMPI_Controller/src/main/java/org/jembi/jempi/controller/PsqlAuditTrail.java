package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jembi.jempi.shared.models.ExpandedAuditEvent;

import java.sql.SQLException;
import java.util.Locale;

import static org.jembi.jempi.shared.models.GlobalConstants.PSQL_TABLE_AUDIT_TRAIL;

final class PsqlAuditTrail {
   private static final Logger LOGGER = LogManager.getLogger(PsqlAuditTrail.class);
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private final PsqlClient psqlClient;

   PsqlAuditTrail() {
      psqlClient = new PsqlClient();
   }

   void createSchemas() {
/*
      LOGGER.debug("Create Schemas");
      psqlClient.connect(AppConfig.POSTGRESQL_AUDIT_DB);
      try (var stmt = psqlClient.createStatement()) {
         stmt.executeUpdate(String.format(
               Locale.ROOT,
               """
               CREATE TABLE IF NOT EXISTS %s (
                   id             UUID         NOT NULL DEFAULT gen_random_uuid(),
                   insertedAt     TIMESTAMP    NOT NULL DEFAULT now(),
                   createdAt      TIMESTAMP    NOT NULL,
                   interactionID  VARCHAR(64),
                   goldenID       VARCHAR(64),
                   event          VARCHAR(256),
                   CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
               );
               """,
               PSQL_TABLE_AUDIT_TRAIL).stripIndent());
         stmt.executeUpdate(String.format(
               Locale.ROOT,
               """
               CREATE INDEX IF NOT EXISTS idx_gid ON %s(goldenID);
               """, PSQL_TABLE_AUDIT_TRAIL).stripIndent());
         stmt.executeUpdate(String.format(
               Locale.ROOT,
               """
               CREATE INDEX IF NOT EXISTS idx_iid ON %s(interactionID);
               """, PSQL_TABLE_AUDIT_TRAIL).stripIndent());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
*/
   }

   void addAuditEvent(final ExpandedAuditEvent expandedAuditEvent) {
      psqlClient.connect(AppConfig.POSTGRESQL_AUDIT_DB);

      try (var preparedStatement = psqlClient.prepareStatement(String.format(Locale.ROOT, """
                                                                                          INSERT INTO %s (createdAt, interactionID, goldenID, event, eventData, eventType)
                                                                                          VALUES (?, ?, ?, ?, ?, ?);
                                                                                          """, PSQL_TABLE_AUDIT_TRAIL)
                                                                     .stripIndent())) {
         var event = expandedAuditEvent.event();
         preparedStatement.setTimestamp(1, event.createdAt());
         preparedStatement.setString(2, event.interactionID());
         preparedStatement.setString(3, event.goldenID());
         preparedStatement.setString(4, event.event());
         preparedStatement.setString(5, expandedAuditEvent.eventData());
         preparedStatement.setString(6, expandedAuditEvent.eventType().name());
         preparedStatement.executeUpdate();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

}
