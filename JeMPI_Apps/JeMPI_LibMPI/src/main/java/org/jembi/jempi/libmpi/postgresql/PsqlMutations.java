package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.LinkInfo;
import org.jembi.jempi.shared.models.SourceId;

import java.sql.SQLException;
import java.util.UUID;

final class PsqlMutations {

   private static final Logger LOGGER = LogManager.getLogger(PsqlMutations.class);
   private static final PsqlClient PSQL_CLIENT = new PsqlClient();
   private static final GoldenRecordDAO GOLDEN_RECORD_DAO = new GoldenRecordDAO();
   private static final SourceIdDAO SOURCE_ID_DAO = new SourceIdDAO();
   private static final EncounterDAO ENCOUNTER_DAO = new EncounterDAO();

   private PsqlMutations() {
   }

   private static UUID insertGoldenRecord(final Interaction interaction) {
      final var sqlGoldenRecord = new GoldenRecordDAO.SqlGoldenRecord(
            null,
            interaction.demographicData().fields.get(0).value(),
            interaction.demographicData().fields.get(1).value(),
            interaction.demographicData().fields.get(2).value(),
            interaction.demographicData().fields.get(3).value(),
            interaction.demographicData().fields.get(4).value(),
            interaction.demographicData().fields.get(5).value(),
            interaction.demographicData().fields.get(6).value(),
            interaction.demographicData().fields.get(7).value(),
            null,
            true,
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      UUID uuid = null;
      try {
         PSQL_CLIENT.connect();
         PSQL_CLIENT.setAutoCommit(false);
         uuid = GOLDEN_RECORD_DAO.insert(PSQL_CLIENT, sqlGoldenRecord);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         PSQL_CLIENT.commit();
      }
      return uuid;
   }

   private static UUID insertSourceId(
         final SourceId sourceId,
         final UUID goldenId) {
      final var sqlSourceId = new SourceIdDAO.SqlSourceId(null,
                                                          sourceId.facility(),
                                                          sourceId.patient(),
                                                          goldenId);
      UUID uid = null;

      PSQL_CLIENT.connect();
      PSQL_CLIENT.setAutoCommit(false);
      try {
         PSQL_CLIENT.connect();
         uid = SOURCE_ID_DAO.insert(PSQL_CLIENT, sqlSourceId);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         PSQL_CLIENT.commit();
      }
      return uid;
   }

   private static UUID insertEncounter(
         final Interaction interaction,
         final UUID goldenId,
         final float score,
         final UUID sourceId) {
      final var sqlEncounter = new EncounterDAO.SqlEncounter(
            null,
            interaction.demographicData().fields.get(0).value(),
            interaction.demographicData().fields.get(1).value(),
            interaction.demographicData().fields.get(2).value(),
            interaction.demographicData().fields.get(3).value(),
            interaction.demographicData().fields.get(4).value(),
            interaction.demographicData().fields.get(5).value(),
            interaction.demographicData().fields.get(6).value(),
            interaction.demographicData().fields.get(7).value(),
            goldenId,
            score,
            sourceId,
            null,
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      UUID uuid = null;
      try {
         PSQL_CLIENT.connect();
         PSQL_CLIENT.setAutoCommit(false);
         uuid = ENCOUNTER_DAO.insert(PSQL_CLIENT, sqlEncounter);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         PSQL_CLIENT.commit();
      }
      return uuid;
   }

   static boolean setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) {
      boolean rs = false;
      try {
         PSQL_CLIENT.connect();
         PSQL_CLIENT.setAutoCommit(false);
         rs = ENCOUNTER_DAO.updateScore(PSQL_CLIENT,
                                        UUID.fromString(interactionUID),
                                        UUID.fromString(goldenRecordUid),
                                        score);
         if (!rs) {
            LOGGER.error("Set score failed: {} --> {} {}", interactionUID, goldenRecordUid, score);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         PSQL_CLIENT.commit();
      }
      return rs;
   }

   static boolean updateField(
         final String goldenRecordUid,
         final String field,
         final String value) {
      boolean rs = false;
      try {
         PSQL_CLIENT.connect();
         PSQL_CLIENT.setAutoCommit(false);
         rs = GOLDEN_RECORD_DAO.updateField(PSQL_CLIENT,
                                            UUID.fromString(goldenRecordUid),
                                            field,
                                            value);
         if (!rs) {
            LOGGER.error("Update Field failed: {} --> {} {}", goldenRecordUid, field, value);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         PSQL_CLIENT.commit();
      }
      return rs;
   }

   static LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) {

      // search for source ID and only if not exist create a new source ID
      final var sourceId = insertSourceId(interaction.sourceId(),
                                          UUID.fromString(goldenIdScore.goldenId()));
      final var encounterId = insertEncounter(
            interaction,
            UUID.fromString(goldenIdScore.goldenId()),
            goldenIdScore.score(),
            sourceId);
      return new LinkInfo(goldenIdScore.goldenId(),
                          encounterId.toString(),
                          sourceId.toString(),
                          goldenIdScore.score());
   }

   static LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      LinkInfo linkInfo = null;

      final var goldenId = insertGoldenRecord(interaction);
      if (goldenId != null) {
         final var sourceId = insertSourceId(interaction.sourceId(), goldenId);
         if (sourceId != null) {
            final var encounterId = insertEncounter(interaction, goldenId, score, sourceId);
            if (encounterId != null) {
               linkInfo = new LinkInfo(goldenId.toString(),
                                       encounterId.toString(),
                                       sourceId.toString(),
                                       score);
            }
         }
      }
      return linkInfo;

   }

   static void connect() {
      PSQL_CLIENT.connect();
   }

}
