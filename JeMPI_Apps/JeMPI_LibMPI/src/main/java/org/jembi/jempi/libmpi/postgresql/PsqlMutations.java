package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.Interaction;
import org.jembi.jempi.shared.models.LinkInfo;
import org.jembi.jempi.shared.models.SourceId;

import java.sql.SQLException;
import java.time.LocalDateTime;
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
            LocalDateTime.now(),
            true,
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      UUID uuid = null;
      try {
         PSQL_CLIENT.connect();
         uuid = GOLDEN_RECORD_DAO.insert(PSQL_CLIENT, sqlGoldenRecord);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
//      } finally {
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
      try {
         PSQL_CLIENT.connect();
         uid = SOURCE_ID_DAO.insert(PSQL_CLIENT, sqlSourceId);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
//      } finally {
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
            interaction.auxInteractionData().auxDateCreated(),
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      UUID uuid = null;
      try {
         PSQL_CLIENT.connect();
         uuid = ENCOUNTER_DAO.insert(PSQL_CLIENT, sqlEncounter);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
//      } finally {
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
         rs = ENCOUNTER_DAO.updateScore(PSQL_CLIENT,
                                        UUID.fromString(interactionUID),
                                        UUID.fromString(goldenRecordUid),
                                        score);
         if (!rs) {
            LOGGER.error("Set score failed: {} --> {} {}", interactionUID, goldenRecordUid, score);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
//      } finally {
      }
      return rs;
   }

   static boolean updateField(
         final String goldenRecordUid,
         final String ccField,
         final String value) {
      boolean rs = false;
      try {
         PSQL_CLIENT.connect();
         rs = GOLDEN_RECORD_DAO.setFieldStringValueById(PSQL_CLIENT,
                                                        UUID.fromString(goldenRecordUid),
                                                        ccField,
                                                        value);
         if (!rs) {
            LOGGER.error("Update Field failed: {} --> {} {}", goldenRecordUid, ccField, value);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
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

   static Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final Float score) {
      LOGGER.debug("linkToNewGoldenRecord");
      try {
         final var sqlEncounter = ENCOUNTER_DAO.getById(PSQL_CLIENT, UUID.fromString(interactionId));
         final var sqlSourceId = SOURCE_ID_DAO.getById(PSQL_CLIENT, sqlEncounter.sourceIdUid());
         final var interaction = ENCOUNTER_DAO.mapToInteraction(sqlEncounter, sqlSourceId);
         final var newGoldenId = insertGoldenRecord(interaction);
         ENCOUNTER_DAO.setFieldUuidValueById(PSQL_CLIENT,
                                             UUID.fromString(interactionId),
                                             "goldenRecordUid",
                                             newGoldenId);
         SOURCE_ID_DAO.setFieldUuidValueById(PSQL_CLIENT,
                                             sqlEncounter.sourceIdUid(),
                                             "goldenRecordUid",
                                             newGoldenId);
         setScore(interactionId, newGoldenId.toString(), score);
         final var count = ENCOUNTER_DAO.countEncountersForGoldenId(PSQL_CLIENT, UUID.fromString(currentGoldenId));
         if (count == 0) {
            GOLDEN_RECORD_DAO.delete(PSQL_CLIENT, UUID.fromString(currentGoldenId));
         }
         return Either.right(new LinkInfo(newGoldenId.toString(), interactionId, sqlSourceId.uid().toString(), score));
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final Float score) {
      LOGGER.debug("updateLink {}", goldenId);
      LOGGER.debug("updateLink {}", newGoldenId);
      LOGGER.debug("updateLink {}", interactionId);
      LOGGER.debug("updateLink {}", score);
      UUID sourceId;
      try {
         sourceId = ENCOUNTER_DAO.getFieldUuidValueById(PSQL_CLIENT, UUID.fromString(interactionId), "sourceIdUid");
      } catch (SQLException e) {
         return Either.left(new MpiServiceError.GoldenIdInteractionConflictError("no sourceId", goldenId, interactionId));
      }
      if (!newGoldenId.equals(goldenId)) {
         try {
            ENCOUNTER_DAO.setFieldUuidValueById(PSQL_CLIENT,
                                                UUID.fromString(interactionId),
                                                "goldenRecordUid",
                                                UUID.fromString(newGoldenId));
            SOURCE_ID_DAO.setFieldUuidValueById(PSQL_CLIENT,
                                                sourceId,
                                                "goldenRecordUid",
                                                UUID.fromString(newGoldenId));
            final var count = ENCOUNTER_DAO.countEncountersForGoldenId(PSQL_CLIENT, UUID.fromString(goldenId));
            if (count == 0) {
               GOLDEN_RECORD_DAO.delete(PSQL_CLIENT, UUID.fromString(goldenId));
            }
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
      }
      return Either.right(new LinkInfo(newGoldenId, interactionId, sourceId.toString(), score));
   }

   static void connect() {
      PSQL_CLIENT.connect();
   }

}
