package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class SourceIdDAO extends GenericDAO<SourceIdDAO.SqlSourceId> {

   private static final Logger LOGGER = LogManager.getLogger(SourceIdDAO.class);

   @Override
   UUID insert(
         final PsqlClient client,
         final SqlSourceId entity) throws SQLException {
      UUID uuid = null;
      final var sql = String.format(
            Locale.ROOT,
            """
            INSERT INTO %s (facility_code,
                            patient_id,
                            golden_record_uid)
            VALUES (?, ?, ?);
            """, getTableName());
      try (var pstmt = client.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         pstmt.setString(1, entity.facilityCode());
         pstmt.setString(2, entity.patientId());
         pstmt.setObject(3, entity.goldenRecordUid());
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
   SqlSourceId getById(
         final PsqlClient client,
         final UUID id) throws SQLException {
      final var sql = "select * from source_id where uid = ?;";

      SourceIdDAO.SqlSourceId entity = null;
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, id);
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            entity = new SourceIdDAO.SqlSourceId(
                  rs.getObject("uid", UUID.class),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getObject(4, UUID.class));
         }
      }
      return entity;
   }

   List<SourceIdDAO.SqlSourceId> getSourceUdsForGoldenId(
         final PsqlClient client,
         final UUID id) throws SQLException {
      final var sql = "select * from source_id where golden_record_uid = ?;";

      List<SourceIdDAO.SqlSourceId> entities = new LinkedList<>();
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, id);
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            final var entity = new SourceIdDAO.SqlSourceId(
                  rs.getObject("uid", UUID.class),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getObject(4, UUID.class));
            entities.add(entity);
         }
      }
      return entities;
   }

   @Override
   protected String getTableName() {
      return "source_id";
   }

   public record SqlSourceId(
         UUID uid,
         String facilityCode,
         String patientId,
         UUID goldenRecordUid) {
   }

}
