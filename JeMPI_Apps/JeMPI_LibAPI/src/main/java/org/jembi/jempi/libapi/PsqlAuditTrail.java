package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.AuditEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.jembi.jempi.shared.models.GlobalConstants.PSQL_TABLE_AUDIT_TRAIL;

final class PsqlAuditTrail {
   private static final Logger LOGGER = LogManager.getLogger(PsqlAuditTrail.class);
   private final PsqlClient psqlClient;

   PsqlAuditTrail(
         final String pgServer,
         final int pgPort,
         final String pgDatabase,
         final String pgUser,
         final String pgPassword) {
      psqlClient = new PsqlClient(pgServer, pgPort, pgDatabase, pgUser, pgPassword);
   }

   List<AuditEvent> goldenRecordAuditTrail(final String uid) {
      psqlClient.connect();
      final var list = new ArrayList<AuditEvent>();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(
            String.format(
                  Locale.ROOT,
                  """
                  SELECT * FROM %s where goldenID = ?;
                  """, PSQL_TABLE_AUDIT_TRAIL).stripIndent())) {
         preparedStatement.setString(1, uid);
         ResultSet rs = preparedStatement.executeQuery();
         while (rs.next()) {
            final var insertedAt = rs.getTimestamp(2);
            final var createdAt = rs.getTimestamp(3);
            final var interactionID = rs.getString(4);
            final var goldenID = rs.getString(5);
            final var event = rs.getString(6);
            list.add(new AuditEvent(createdAt, insertedAt, interactionID, goldenID, event));
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   List<AuditEvent> interactionRecordAuditTrail(final String uid) {
      psqlClient.connect();
      final var list = new ArrayList<AuditEvent>();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(
            String.format(
                  Locale.ROOT,
                  """
                  SELECT * FROM %s where interactionID = ?;
                  """, PSQL_TABLE_AUDIT_TRAIL).stripIndent())) {
         preparedStatement.setString(1, uid);
         ResultSet rs = preparedStatement.executeQuery();
         while (rs.next()) {
            final var insertedAt = rs.getTimestamp(2);
            final var createdAt = rs.getTimestamp(3);
            final var interactionID = rs.getString(4);
            final var goldenID = rs.getString(5);
            final var event = rs.getString(6);
            list.add(new AuditEvent(createdAt, insertedAt, interactionID, goldenID, event));
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

}
