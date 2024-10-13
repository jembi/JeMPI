package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

abstract class GenericDAO<T> {

   private static final Logger LOGGER = LogManager.getLogger(GenericDAO.class);

   // Method to create a new record
   abstract UUID insert(
         PsqlClient client,
         T entity) throws SQLException;

   // Method to retrieve a record by its ID
   abstract T getById(
         PsqlClient client,
         UUID id) throws SQLException;

   // Method to retrieve all records (can be overridden for specific implementations)
   abstract List<T> getAll(PsqlClient client) throws SQLException;

   // Method to update a record
   abstract void update(
         PsqlClient client,
         T entity) throws SQLException;

   // Method to delete a record by its ID
   void delete(
         final PsqlClient client,
         final UUID uuid) throws SQLException {
      String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, uuid);
         pstmt.executeUpdate();
         System.out.println("Record deleted successfully");
      }
   }

   long count(final PsqlClient client) throws SQLException {
      final String sql = "SELECT COUNT(*) FROM " + getTableName();
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         final var rs = pstmt.executeQuery();
         if (rs.next()) {
            return rs.getLong(1);
         }
      }
      return -1;
   }

   List<UUID> getUid(final PsqlClient client) throws SQLException {
      final String sql = "SELECT uid FROM " + getTableName();
      final List<UUID> result = new LinkedList<>();
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         final var rs = pstmt.executeQuery();
         while (rs.next()) {
            result.add(rs.getObject(1, UUID.class));
         }
      }
      return result;
   }

   // Abstract method to specify the table name (implemented by subclasses)
   protected abstract String getTableName();

   // Utility method to close connections (optional)
   protected void close(final AutoCloseable resource) {
      if (resource != null) {
         try {
            resource.close();
         } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
   }

}
