package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.*;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class PsqlQueries {
   private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);

   private static final PsqlClient PSQL_CLIENT = new PsqlClient();
   private static final GoldenRecordDAO GOLDEN_RECORD_DAO = new GoldenRecordDAO();
   private static final EncounterDAO ENCOUNTER_DAO = new EncounterDAO();
   private static final SourceIdDAO SOURCE_ID_DAO = new SourceIdDAO();

   private PsqlQueries() {
   }

   static void connect() {
      PSQL_CLIENT.connect();
   }

   private static GoldenRecord getGoldenRecord(final String uid) {
      GoldenRecord goldenRecord = null;
      try {
         PSQL_CLIENT.connect();
         final var sqlGoldenRecord = GOLDEN_RECORD_DAO.getById(PSQL_CLIENT, UUID.fromString(uid));
         final var demographicData = new DemographicData();
         for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
            demographicData.fields.add(
                  new DemographicData.DemographicField(
                        Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                        sqlGoldenRecord.getDemographicField(i)));
         }
         List<AuxGoldenRecordData.AuxGoldenRecordUserField> auxUserFields = new LinkedList<>();
         auxUserFields.add(new AuxGoldenRecordData.AuxGoldenRecordUserField(
               Config.FIELDS_CONFIG.userAuxGoldenRecordFields.getFirst().ccName(), sqlGoldenRecord.auxId()));
         final var auxGoldenRecordData = new AuxGoldenRecordData(
               null,
               sqlGoldenRecord.auxAutoUpdate(),
               auxUserFields);

         final var sidList = SOURCE_ID_DAO.getSourceUdsForGoldenId(PSQL_CLIENT, UUID.fromString(uid));

         goldenRecord = new GoldenRecord(
               uid,
               sidList.stream().map(sid -> new SourceId(sid.uid().toString(), sid.facilityCode(), sid.patientId())).toList(),
               auxGoldenRecordData,
               demographicData);
      } catch (SQLException e) {
         LOGGER.error(e.getMessage(), e);
      }
      return goldenRecord;
   }

   private static List<InteractionWithScore> getInteractionsWithScore(final String uid) {
      try {
         PSQL_CLIENT.connect();
         final var sqlEncounters = ENCOUNTER_DAO.getEncountersForGoldenId(PSQL_CLIENT, UUID.fromString(uid));
         return sqlEncounters.stream()
                             .map(sqlEncounter -> {
                                final var demographicData = new DemographicData();
                                for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
                                   demographicData.fields.add(
                                         new DemographicData.DemographicField(
                                               Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                                               sqlEncounter.getDemographicField(i)));
                                }
                                SourceId sourceId = null;
                                try {
                                   final var sid = SOURCE_ID_DAO.getById(PSQL_CLIENT, sqlEncounter.sourceIdUid());
                                   sourceId = new SourceId(sid.uid().toString(), sid.facilityCode(), sid.patientId());
                                } catch (SQLException e) {
                                   LOGGER.error(e.getLocalizedMessage(), e);
                                }
                                return new InteractionWithScore(new Interaction(sqlEncounter.uid().toString(),
                                                                                sourceId,
                                                                                sqlEncounter.getAuxInteractionData(),
                                                                                demographicData),
                                                                sqlEncounter.score());
                             })
                             .toList();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return List.of();
   }

   static long countInteractions() {
      PSQL_CLIENT.connect();
      try {
         return ENCOUNTER_DAO.count(PSQL_CLIENT);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return -1;
   }

   static long countGoldenRecords() {
      PSQL_CLIENT.connect();
      try {
         return GOLDEN_RECORD_DAO.count(PSQL_CLIENT);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return -1;
   }

   static List<String> findGoldenIds() {
      PSQL_CLIENT.connect();
      try {
         return GOLDEN_RECORD_DAO.getUid(PSQL_CLIENT).stream().map(UUID::toString).toList();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return List.of();
   }

   static PaginatedResultSet<ExpandedGoldenRecord> findExpandedGoldenRecords(final List<String> goldenIds) {
      final List<ExpandedGoldenRecord> list = new LinkedList<>();
      for (String goldenId : goldenIds) {
         final var goldenRecord = getGoldenRecord(goldenId);
         final var interactionsWithScore = getInteractionsWithScore(goldenId);
         final var expandedGoldenRecord = new ExpandedGoldenRecord(goldenRecord, interactionsWithScore);
         list.add(expandedGoldenRecord);
      }
      final var result = new PaginatedResultSet<>(list, List.of(new LibMPIPagination(list.size())));
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(result);
         LOGGER.debug(json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return result;
   }

   static List<GoldenRecord> findLinkCandidates(final DemographicData demographicData) {
      final var list = new LinkedList<GoldenRecord>();
      PSQL_CLIENT.connect();
      final List<GoldenRecordDAO.SqlGoldenRecord> candidates;
      try {
         candidates = GOLDEN_RECORD_DAO.findLinkCandidates(PSQL_CLIENT, demographicData);
         for (GoldenRecordDAO.SqlGoldenRecord candidate : candidates) {
            final var demographicFields = new DemographicData();
            for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
               demographicFields.fields.add(
                     new DemographicData.DemographicField(
                           Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                           candidate.getDemographicField(i)));
            }
            final List<AuxGoldenRecordData.AuxGoldenRecordUserField> auxGoldenRecordUserFields = new LinkedList<>();
            auxGoldenRecordUserFields.add(new AuxGoldenRecordData.AuxGoldenRecordUserField("aux_id", candidate.auxId()));
            final var auxGoldenRecordData = new AuxGoldenRecordData(
                  candidate.auxDateCreated(),
                  candidate.auxAutoUpdate(),
                  auxGoldenRecordUserFields
            );
            final var goldenRecord = new GoldenRecord(candidate.uid().toString(),
                                                      null,
                                                      auxGoldenRecordData,
                                                      demographicFields);
            list.add(goldenRecord);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return list;
   }

}
