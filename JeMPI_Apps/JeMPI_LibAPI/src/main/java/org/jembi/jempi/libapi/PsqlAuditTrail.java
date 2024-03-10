package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.models.LinkingAuditEventData;
import org.jembi.jempi.shared.utils.AuditTrailUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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

   List<ApiModels.ApiAuditTrail.LinkingAuditEntry> goldenRecordAuditTrail(final String uid) {
      psqlClient.connect();
      final var list = new ArrayList<ApiModels.ApiAuditTrail.LinkingAuditEntry>();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(
              String.format(Locale.ROOT, "SELECT * FROM %s WHERE eventType = ?  AND eventData like CONCAT( '%%',?,'%%')", PSQL_TABLE_AUDIT_TRAIL)
      )) {
         preparedStatement.setString(1, GlobalConstants.AuditEventType.LINKING_EVENT.name());
         preparedStatement.setString(2, uid);
         ResultSet rs = preparedStatement.executeQuery();
         while (rs.next()) {
            final var insertTime = rs.getString(2);
            final var createdTime = rs.getString(3);
            final  var eventType = rs.getString(4);
            final var eventData = rs.getString(5);

            if (Objects.equals(eventType, GlobalConstants.AuditEventType.LINKING_EVENT.name())) {
               LinkingAuditEventData deserializeEventData = AuditTrailUtil.getDeserializeEventData(eventData, LinkingAuditEventData.class);
               if (!Objects.equals(deserializeEventData.goldenID(), uid)) {
                 continue;
               }
               list.add(new ApiModels.ApiAuditTrail.LinkingAuditEntry(
                       insertTime,
                       createdTime,
                       deserializeEventData.interaction_id(),
                       deserializeEventData.goldenID(),
                       deserializeEventData.message(),
                       deserializeEventData.score(),
                       deserializeEventData.linkingRule().name()
               ));
            }
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

   List<ApiModels.ApiAuditTrail.LinkingAuditEntry> interactionRecordAuditTrail(final String uid) {
      psqlClient.connect();
      final var list = new ArrayList<ApiModels.ApiAuditTrail.LinkingAuditEntry>();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(
              String.format(Locale.ROOT, "SELECT * FROM %s WHERE eventType = ?  AND eventData like CONCAT('%%',?,'%%')", PSQL_TABLE_AUDIT_TRAIL)
      )) {
         preparedStatement.setString(1, GlobalConstants.AuditEventType.LINKING_EVENT.name());
         preparedStatement.setString(2, uid);
         ResultSet rs = preparedStatement.executeQuery();
         while (rs.next()) {
            final var insertTime = rs.getString(2);
            final var createdTime = rs.getString(3);
            final  var eventType = rs.getString(4);
            final var eventData = rs.getString(5);

            if (Objects.equals(eventType, GlobalConstants.AuditEventType.LINKING_EVENT.name())) {
               LinkingAuditEventData deserializeEventData = AuditTrailUtil.getDeserializeEventData(eventData, LinkingAuditEventData.class);
               if (!Objects.equals(deserializeEventData.interaction_id(), uid)) {
                  continue;
               }
               list.add(new ApiModels.ApiAuditTrail.LinkingAuditEntry(
                       insertTime,
                       createdTime,
                       deserializeEventData.interaction_id(),
                       deserializeEventData.goldenID(),
                       deserializeEventData.message(),
                       deserializeEventData.score(),
                       deserializeEventData.linkingRule().name()
               ));
            }
         }
      } catch (Exception e) {
         LOGGER.error(e);
      }
      return list;
   }

}
