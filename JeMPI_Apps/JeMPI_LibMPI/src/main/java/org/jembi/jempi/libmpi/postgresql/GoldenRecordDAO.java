package org.jembi.jempi.libmpi.postgresql;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.sql.*;
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
                            aux_date_created,
                            aux_auto_update_enabled,
                            aux_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?);
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
         pstmt.setTimestamp(9, Timestamp.valueOf(entity.auxDateCreated));
         pstmt.setBoolean(10, entity.auxAutoUpdate());
         pstmt.setString(11, entity.auxId());
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

   List<UUID> getPaginatedUID(
         final PsqlClient client,
         final long offset,
         final long limit,
         final String sortBy,
         final Boolean sortAsc) throws SQLException {
      final var sql = String.format("select uid from golden_records order by %s %s OFFSET %d LIMIT %d;",
                                    AppUtils.camelToSnake(sortBy),
                                    Boolean.TRUE.equals(sortAsc)
                                          ? "asc"
                                          : "desc",
                                    offset,
                                    limit);
      final var list = new LinkedList<UUID>();
      try (PreparedStatement preparedStatement = client.prepareStatement(sql)) {

         final var rs = preparedStatement.executeQuery();
         while (rs.next()) {
            list.add(rs.getObject("uid", UUID.class));
         }
      }
      return list;
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
                  rs.getTimestamp(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(0).scName()).toLocalDateTime(),
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
      final var pin = demographicData.fields.get(Config.FIELDS_CONFIG.findIndexOfDemographicField("pin")).value();
      if (!(StringUtils.isBlank(pin) || "999999999999999".equals(pin))) {
         final var sql = "select * from golden_records where pin = ?;";
         try (PreparedStatement preparedStatement = client.prepareStatement(sql)) {
            preparedStatement.setString(1, demographicData.fields.get(DEMOGRAPHIC_IDX_PIN).value());

            final var rs = preparedStatement.executeQuery();
            while (rs.next()) {
               list.add(new SqlGoldenRecord(
                     rs.getObject("uid", java.util.UUID.class),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(0).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(1).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(2).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(3).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(4).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(5).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(6).scName()),
                     rs.getString(Config.FIELDS_CONFIG.demographicFields.get(7).scName()),
                     rs.getTimestamp(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(0).scName()).toLocalDateTime(),
                     rs.getBoolean(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(1).scName()),
                     rs.getString(Config.FIELDS_CONFIG.userAuxGoldenRecordFields.getFirst().scName())));
            }
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
                      similarity(first_name,?)    > 0.6  and  similarity(middle_name,?) > 0.6  or
                      similarity(first_name,?)    > 0.6  and  similarity(surname,?)     > 0.6  or
                      similarity(middle_name,?)   > 0.6  and  similarity(surname,?)     > 0.6  or
                      similarity(dob,?)           > 0.8                                        or
                      similarity(cell_phone,?)    > 0.8                                        or
                      similarity(chiefdom_code,?) > 0.8  and  similarity(cell_phone,?)  > 0.8;
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
         pstmt.setString(9, demographicData.fields.get(DEMOGRAPHIC_IDX_CHIEFDOM).value());
         pstmt.setString(10, demographicData.fields.get(DEMOGRAPHIC_IDX_CELL_PHONE).value());

         final var rs = pstmt.executeQuery();
         while (rs.next()) {
            list.add(new SqlGoldenRecord(
                  rs.getObject("uid", java.util.UUID.class),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(0).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(2).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(3).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(4).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(5).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(6).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(7).scName()),
                  rs.getTimestamp(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(0).scName()).toLocalDateTime(),
                  rs.getBoolean(Config.FIELDS_CONFIG.auxGoldenRecordFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.userAuxGoldenRecordFields.getFirst().scName())));
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
              auxDateCreated,
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
