package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.models.AuditEvent;

import java.sql.SQLException;
import java.util.Locale;

import static org.jembi.jempi.shared.models.GlobalConstants.PSQL_TABLE_AUDIT_TRAIL;

final class PsqlAuditTrail {
   private static final Logger LOGGER = LogManager.getLogger(PsqlAuditTrail.class);
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

   void addAuditEvent(final AuditEvent auditEvent) {
      psqlClient.connect(AppConfig.POSTGRESQL_AUDIT_DB);

      try (var preparedStatement = psqlClient.prepareStatement(String.format(Locale.ROOT, """
                                                                                          INSERT INTO %s (createdAt, eventType, eventData)
                                                                                          VALUES (?, ?, ?);
                                                                                          """, PSQL_TABLE_AUDIT_TRAIL)
                                                                      .stripIndent())) {
         preparedStatement.setTimestamp(1, auditEvent.createdAt());
         preparedStatement.setString(2, auditEvent.eventType().name());
         preparedStatement.setString(3, auditEvent.eventData());
         preparedStatement.executeUpdate();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

}
