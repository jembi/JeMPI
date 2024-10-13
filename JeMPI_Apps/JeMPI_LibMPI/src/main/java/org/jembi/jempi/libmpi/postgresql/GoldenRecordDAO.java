package org.jembi.jempi.libmpi.postgresql;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.jembi.jempi.libmpi.postgresql.PsqlCommon.*;

public final class GoldenRecordDAO extends GenericDAO<GoldenRecordDAO.SqlGoldenRecord> {

   private static final Logger LOGGER = LogManager.getLogger(GoldenRecordDAO.class);

   @Override
   UUID insert(
         final PsqlClient client,
         final SqlGoldenRecord entity) throws SQLException {
      UUID uuid = null;
      final var sql = String.format(
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
                            aux_auto_update_enabled,
                            aux_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """, getTableName());
      try (var pstmt = client.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
            final var value = entity.getDemographicField(i);
            if (StringUtils.isBlank(value)) {
               pstmt.setString(i + 1, StringUtils.EMPTY);
            } else {
               pstmt.setString(i + 1, entity.getDemographicField(i));
            }
         }
         pstmt.setBoolean(9, entity.auxAutoUpdate());
         pstmt.setString(10, entity.auxId());
         final var affectedRows = pstmt.executeUpdate();
         if (affectedRows > 0) {
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
               if (rs != null && rs.next()) {
                  uuid = UUID.fromString(rs.getString("uid"));
               } else {
                  LOGGER.error("No generated keys");
               }
            }
         }
      }
      return uuid;
   }

   @Override
   SqlGoldenRecord getById(
         final PsqlClient client,
         final UUID id) throws SQLException {

      final var sql = "select * from golden_records where uid = ?;";

      SqlGoldenRecord entity = null;
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, id);
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            entity = new SqlGoldenRecord(
                  rs.getObject("uid", UUID.class),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(0).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(2).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(3).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(4).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(5).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(6).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(7).scName()),
                  null,
                  rs.getBoolean(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.userAuxGoldenRecordFields.getFirst().scName()));
         }
      }
      return entity;
   }

   List<SqlGoldenRecord> findDeterministicLinkCandidates(
         final PsqlClient client,
         final DemographicData demographicData) throws SQLException {
      final var list = new LinkedList<SqlGoldenRecord>();
      final var sql = """
                      select * from golden_records where pin = ?;
                      """.stripIndent();
      try (PreparedStatement preparedStatement = client.prepareStatement(sql)) {
         preparedStatement.setString(1, demographicData.fields.get(DEMOGRAPHIC_IDX_PIN).value());

         final var rs = preparedStatement.executeQuery();
         while (rs.next()) {
            list.add(new SqlGoldenRecord(
                  rs.getObject("uid", java.util.UUID.class),
                  rs.getString("first_name"),
                  rs.getString("middle_name"),
                  rs.getString("surname"),
                  rs.getString("dob"),
                  rs.getString("sex"),
                  rs.getString("chiefdom_code"),
                  rs.getString("cell_phone"),
                  rs.getString("pin"),
                  null,
                  rs.getBoolean("aux_auto_update_enabled"),
                  rs.getString("aux_id")));
         }
      }
      return list;
   }

   List<SqlGoldenRecord> findProbabilisticLinkCandidates(
         final PsqlClient client,
         final DemographicData demographicData) throws SQLException {
      final var list = new LinkedList<SqlGoldenRecord>();
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
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setString(1, demographicData.fields.get(DEMOGRAPHIC_IDX_FIRST_NAME).value());
         pstmt.setString(2, demographicData.fields.get(DEMOGRAPHIC_IDX_MIDDLE_NAME).value());
         pstmt.setString(3, demographicData.fields.get(DEMOGRAPHIC_IDX_FIRST_NAME).value());
         pstmt.setString(4, demographicData.fields.get(DEMOGRAPHIC_IDX_SURNAME).value());
         pstmt.setString(5, demographicData.fields.get(DEMOGRAPHIC_IDX_MIDDLE_NAME).value());
         pstmt.setString(6, demographicData.fields.get(DEMOGRAPHIC_IDX_SURNAME).value());
         pstmt.setString(7, demographicData.fields.get(DEMOGRAPHIC_IDX_DOB).value());
         pstmt.setString(8, demographicData.fields.get(DEMOGRAPHIC_IDX_CELL_PHONE).value());
         pstmt.setString(9, demographicData.fields.get(DEMOGRAPHIC_IDX_PIN).value());
         pstmt.setString(10, demographicData.fields.get(DEMOGRAPHIC_IDX_CHIEFDOM).value());
         pstmt.setString(11, demographicData.fields.get(DEMOGRAPHIC_IDX_CELL_PHONE).value());

         final var rs = pstmt.executeQuery();
         while (rs.next()) {
            list.add(new SqlGoldenRecord(
                  rs.getObject("uid", java.util.UUID.class),
                  rs.getString("first_name"),
                  rs.getString("middle_name"),
                  rs.getString("surname"),
                  rs.getString("dob"),
                  rs.getString("sex"),
                  rs.getString("chiefdom_code"),
                  rs.getString("cell_phone"),
                  rs.getString("pin"),
                  null,
                  rs.getBoolean("aux_auto_update_enabled"),
                  rs.getString("aux_id")));
         }
      }
      return list;
   }

   List<SqlGoldenRecord> findLinkCandidates(
         final PsqlClient client,
         final DemographicData demographicData) throws SQLException {
      final var candidates = findDeterministicLinkCandidates(client, demographicData);
      if (!candidates.isEmpty()) {
         return candidates;
      } else {
         return findProbabilisticLinkCandidates(client, demographicData);
      }
   }

   boolean updateField(
         final PsqlClient client,
         final UUID uid,
         final String field,
         final String value) throws SQLException {
      final String sql = String.format(Locale.ROOT,
                                       "UPDATE golden_records SET %s = ? WHERE uid = ?;",
                                       AppUtils.camelToSnake(field));
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setString(1, value);
         pstmt.setObject(2, uid);
         final var rs = pstmt.executeUpdate();
         client.commit();
         return rs == 1;
      }
   }

   @Override
   List<SqlGoldenRecord> getAll(final PsqlClient client) throws SQLException {
      return List.of();
   }

   @Override
   void update(
         final PsqlClient client,
         final SqlGoldenRecord entity) throws SQLException {
   }

   @Override
   protected String getTableName() {
      return "golden_records";
   }

   public record SqlGoldenRecord(
         UUID uid,
         String firstName,
         String middleName,
         String surname,
         String dob,
         String sex,
         String chiefdomCode,
         String cellPhone,
         String pin,
         java.time.LocalDateTime auxDateCreated,
         Boolean auxAutoUpdate,
         String auxId) {

      SqlGoldenRecord(
            final List<String> demographicFields,
            final java.time.LocalDateTime auxDateCreated,
            final Boolean auxAutoUpdate,
            final List<String> userAuxFields) {
         this(null,
              demographicFields.get(0),
              demographicFields.get(1),
              demographicFields.get(2),
              demographicFields.get(3),
              demographicFields.get(4),
              demographicFields.get(5),
              demographicFields.get(6),
              demographicFields.get(7),
              null,
              auxAutoUpdate,
              userAuxFields.getFirst()
             );
      }

      String getDemographicField(final int index) {
         return switch (index) {
            case 0 -> firstName();
            case 1 -> middleName();
            case 2 -> surname();
            case 3 -> dob();
            case 4 -> sex();
            case 5 -> chiefdomCode();
            case 6 -> cellPhone();
            case 7 -> pin();
            default -> throw new IllegalArgumentException();
         };
      }
   }

}
