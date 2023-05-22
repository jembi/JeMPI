package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;

import static org.jembi.jempi.libmpi.postgresql.PostgresqlMutations.TABLE_EDGES;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class Edge {

   private static final Logger LOGGER = LogManager.getLogger(Edge.class);

   private Edge() {
   }

   static void createEdge(
         final UUID uid1,
         final UUID uid2,
         final EdgeName edgeName) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format(Locale.ROOT,
                          """
                          INSERT INTO %s (source, dest, name) VALUES ('%s', '%s', '%s');
                          """,
                          TABLE_EDGES,
                          uid1.toString(), uid2.toString(), edgeName.name()).stripIndent()
                  .stripIndent(),
            Statement.RETURN_GENERATED_KEYS)
      ) {
         stmt.executeUpdate();
         try (ResultSet keys = stmt.getGeneratedKeys()) {
            keys.next();
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   static void createEdge(
         final UUID uid1,
         final UUID uid2,
         final EdgeName edgeName,
         final Facet facet) {
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format(Locale.ROOT,
                          """
                          INSERT INTO %s (source, dest, name, facet) VALUES ('%s', '%s', '%s', '%s');
                          """,
                          TABLE_EDGES,
                          uid1.toString(),
                          uid2.toString(),
                          edgeName.name(),
                          OBJECT_MAPPER.writeValueAsString(facet))
                  .stripIndent(),
            Statement.RETURN_GENERATED_KEYS)
      ) {
         stmt.executeUpdate();
         try (ResultSet keys = stmt.getGeneratedKeys()) {
            keys.next();
         }
      } catch (SQLException | JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   enum EdgeName {IID2SID, GID2SID, GID2IID}

}
