package org.jembi.jempi.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.AuditEvent;

import java.sql.SQLException;

import static org.jembi.jempi.shared.models.GlobalConstants.PSQL_TABLE_AUDIT_TRAIL;

final class PsqlAuditTrail {
   private static final Logger LOGGER = LogManager.getLogger(PsqlAuditTrail.class);
   private final String pgDatabase;
   private final String pgUser;
   private final String pgPassword;
   private final PsqlClient psqlClient;

   PsqlAuditTrail(
         final String database,
         final String user,
         final String password) {
      pgDatabase = database;
      pgUser = user;
      pgPassword = password;
      psqlClient = new PsqlClient();
   }

   void createSchemas() {
      LOGGER.debug("Create Schemas");
      psqlClient.connect(pgDatabase, pgUser, pgPassword);
      try (var stmt = psqlClient.createStatement()) {
         stmt.executeUpdate(String.format(
               """
               CREATE TABLE IF NOT EXISTS %s (
                   id          UUID           NOT NULL DEFAULT gen_random_uuid(),
                   inserted_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
                   created_at  TIMESTAMPTZ    NOT NULL,
                   uid         VARCHAR(256),
                   event       VARCHAR(256),
                   CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
               );
               """,
               PSQL_TABLE_AUDIT_TRAIL).stripIndent());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void addAuditEvent(final AuditEvent event) {
      LOGGER.debug("{}", event);
   }

}
