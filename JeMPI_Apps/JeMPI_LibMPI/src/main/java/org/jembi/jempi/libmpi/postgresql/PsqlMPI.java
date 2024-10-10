package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class PsqlMPI {
   private static final Logger LOGGER = LogManager.getLogger(PsqlMPI.class);
   private static final String PSQL_TABLE_GOLDEN_RECORDS = "golden_records";
   private static final String PSQL_TABLE_SOURCE_ID = "source_id";
   private static final String PSQL_TABLE_ENCOUNTERS = "encounters";
   private static final int FIRST_NAME_IDX = 0;
   private static final int MIDDLE_NAME_IDX = 1;
   private static final int SURNAME_IDX = 2;
   private static final int DOB_IDX = 3;
   private static final int SEX_IDX = 4;
   private static final int CHIEFDOM_IDX = 5;
   private static final int CELL_PHONE_IDX = 6;
   private static final int PIN_IDX = 7;
   private final PsqlClient psqlClient;

   PsqlMPI() {
      psqlClient = new PsqlClient();
   }

   void connect() {
      psqlClient.connect();
   }

   List<GoldenRecord> findLinkCandidates(final DemographicData demographicData) {
      try {
         LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(demographicData));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage());
      }

      psqlClient.connect();
      final var list = new LinkedList<GoldenRecord>();
      final var sql = """
                      select * from golden_records where
                      levenshtein(first_name, ?) <= 3 and levenshtein(middle_name, ?) <= 3 or
                      levenshtein(first_name, ?) <= 3 and levenshtein(surname, ?) <= 3 or
                      levenshtein(middle_name, ?) <= 3 and levenshtein(surname, ?) <= 3 or
                      levenshtein(dob, ?) <= 2 or
                      levenshtein(cell_phone, ?) <= 2 or
                      levenshtein(pin, ?) <= 3 or
                      levenshtein(chiefdom_code, ?) <= 3 and levenshtein(cell_phone, ?) <= 3;
                      """.stripIndent();
      try (PreparedStatement preparedStatement = psqlClient.prepareStatement(sql)) {
         preparedStatement.setString(1, demographicData.fields.get(FIRST_NAME_IDX).value());
         preparedStatement.setString(2, demographicData.fields.get(MIDDLE_NAME_IDX).value());
         preparedStatement.setString(3, demographicData.fields.get(FIRST_NAME_IDX).value());
         preparedStatement.setString(4, demographicData.fields.get(SURNAME_IDX).value());
         preparedStatement.setString(5, demographicData.fields.get(MIDDLE_NAME_IDX).value());
         preparedStatement.setString(6, demographicData.fields.get(SURNAME_IDX).value());
         preparedStatement.setString(7, demographicData.fields.get(DOB_IDX).value());
         preparedStatement.setString(8, demographicData.fields.get(CELL_PHONE_IDX).value());
         preparedStatement.setString(9, demographicData.fields.get(PIN_IDX).value());
         preparedStatement.setString(10, demographicData.fields.get(CHIEFDOM_IDX).value());
         preparedStatement.setString(11, demographicData.fields.get(CELL_PHONE_IDX).value());

         ResultSet rs = preparedStatement.executeQuery();
         while (rs.next()) {
            final var work = new DemographicData();
            work.fields.add(new DemographicData.DemographicField("firstName", rs.getString(2)));
            work.fields.add(new DemographicData.DemographicField("middleName", rs.getString(3)));
            work.fields.add(new DemographicData.DemographicField("surname", rs.getString(4)));
            work.fields.add(new DemographicData.DemographicField("dob", rs.getString(5)));
            work.fields.add(new DemographicData.DemographicField("sex", rs.getString(6)));
            work.fields.add(new DemographicData.DemographicField("chiefdom", rs.getString(7)));
            work.fields.add(new DemographicData.DemographicField("cellPhone", rs.getString(8)));
            work.fields.add(new DemographicData.DemographicField("pin", rs.getString(9)));
            final var goldenRecord = new GoldenRecord(rs.getString(1),
                                                      null,
                                                      new AuxGoldenRecordData(null,
                                                                              true,
                                                                              null),
                                                      work);
            list.add(goldenRecord);
         }
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }

      return list;
   }

   private UUID insertGoldenRecord(final Interaction interaction) {
      UUID uid = null; // GoldenId
      psqlClient.connect();

      try (var preparedStatement =
                 psqlClient.prepareStatement(
                       String.format(
                                   Locale.ROOT,
                                   """
                                   INSERT INTO %s (first_name,
                                                   middle_name,
                                                   surname,
                                                   dob,
                                                   sex,
                                                   chiefdom_code,
                                                   cell_phone,
                                                   pin,
                                                   aux_id)
                                   VALUES (?,?,?,?,?,?,?,?,?);
                                   """, PSQL_TABLE_GOLDEN_RECORDS)
                             .stripIndent(),
                       Statement.RETURN_GENERATED_KEYS)) {
         preparedStatement.setString(1, interaction.demographicData().fields.get(0).value());
         preparedStatement.setString(2, interaction.demographicData().fields.get(1).value());
         preparedStatement.setString(3, interaction.demographicData().fields.get(2).value());
         preparedStatement.setString(4, interaction.demographicData().fields.get(3).value());
         preparedStatement.setString(5, interaction.demographicData().fields.get(4).value());
         preparedStatement.setString(6, interaction.demographicData().fields.get(5).value());
         preparedStatement.setString(7, interaction.demographicData().fields.get(6).value());
         preparedStatement.setString(8, interaction.demographicData().fields.get(7).value());
         preparedStatement.setString(9, interaction.auxInteractionData().auxUserFields().get(0).value());
         int affectedRows = preparedStatement.executeUpdate();
         if (affectedRows > 0) {
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
               if (rs != null && rs.next()) {
                  uid = UUID.fromString(rs.getString("uid"));
               } else {
                  LOGGER.error("No generated keys");
               }
            }
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         psqlClient.setAutoCommit(true);
      }
      return uid;
   }

   private UUID insertSourceId(
         final SourceId sourceId,
         final UUID goldenId) {
      UUID uid = null;
      psqlClient.connect();

      try (var preparedStatement =
                 psqlClient.prepareStatement(
                       String.format(
                                   Locale.ROOT,
                                   """
                                   INSERT INTO %s (facility_code,
                                                   patient_id,
                                                   golden_record_uid)
                                   VALUES (?, ?, ?);
                                   """, PSQL_TABLE_SOURCE_ID)
                             .stripIndent(),
                       Statement.RETURN_GENERATED_KEYS)) {
         preparedStatement.setString(1, sourceId.facility());
         preparedStatement.setString(2, sourceId.patient());
         preparedStatement.setObject(3, goldenId);
         int affectedRows = preparedStatement.executeUpdate();
         if (affectedRows > 0) {
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
               if (rs != null && rs.next()) {
                  uid = UUID.fromString(rs.getString("uid"));
               } else {
                  LOGGER.error("No generated keys");
               }
            }
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         psqlClient.setAutoCommit(true);
      }
      return uid;
   }

   private UUID insertEncounter(
         final Interaction interaction,
         final UUID goldenId,
         final UUID sourceId,
         final float score) {
      UUID uid = null; // EncounterId
      psqlClient.connect();

      try (var preparedStatement =
                 psqlClient.prepareStatement(
                       String.format(
                                   Locale.ROOT,
                                   """
                                   INSERT INTO %s (first_name,
                                                   middle_name,
                                                   surname,
                                                   dob,
                                                   sex,
                                                   chiefdom_code,
                                                   cell_phone,
                                                   pin,
                                                   golden_record_uid,
                                                   score,
                                                   source_id_uid,
                                                   aux_id)
                                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                                   """, PSQL_TABLE_ENCOUNTERS)
                             .stripIndent(),
                       Statement.RETURN_GENERATED_KEYS)) {
         preparedStatement.setString(1, interaction.demographicData().fields.get(0).value());
         preparedStatement.setString(2, interaction.demographicData().fields.get(1).value());
         preparedStatement.setString(3, interaction.demographicData().fields.get(2).value());
         preparedStatement.setString(4, interaction.demographicData().fields.get(3).value());
         preparedStatement.setString(5, interaction.demographicData().fields.get(4).value());
         preparedStatement.setString(6, interaction.demographicData().fields.get(5).value());
         preparedStatement.setString(7, interaction.demographicData().fields.get(6).value());
         preparedStatement.setString(8, interaction.demographicData().fields.get(7).value());
         preparedStatement.setObject(9, goldenId);
         preparedStatement.setFloat(10, score);
         preparedStatement.setObject(11, sourceId);
         preparedStatement.setString(12, interaction.auxInteractionData().auxUserFields().get(0).value());
         int affectedRows = preparedStatement.executeUpdate();
         if (affectedRows > 0) {
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
               if (rs != null && rs.next()) {
                  uid = UUID.fromString(rs.getString("uid"));
               } else {
                  LOGGER.error("No generated keys");
               }
            }
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      } finally {
         psqlClient.setAutoCommit(true);
      }
      return uid;
   }


   LinkInfo createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      LinkInfo linkInfo = null;

      final var goldenId = insertGoldenRecord(interaction);
      if (goldenId != null) {
         final var sourceId = insertSourceId(interaction.sourceId(), goldenId);
         if (sourceId != null) {
            final var encounterId = insertEncounter(interaction, goldenId, sourceId, score);
            if (encounterId != null) {
               linkInfo = new LinkInfo(goldenId.toString(), encounterId.toString(), sourceId.toString(), score);
            }
         }
      }
      return linkInfo;

   }

}
