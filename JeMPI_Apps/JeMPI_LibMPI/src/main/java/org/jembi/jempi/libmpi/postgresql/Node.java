package org.jembi.jempi.libmpi.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.jembi.jempi.libmpi.postgresql.PostgresqlMutations.TABLE_NODES;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

interface Node {

   Logger I_LOGGER = LogManager.getLogger(Node.class);

   NodeType getType();

   NodeData getNodeData();

   default UUID createNode() {
      UUID uid;
      try (var stmt = PostgresqlClient.getInstance().prepareStatement(
            String.format("""
                          insert into %s (type, fields)
                          values ('%s', '%s');
                          """,
                          TABLE_NODES,
                          this.getType().name(),
                          OBJECT_MAPPER.writeValueAsString(this.getNodeData())).stripIndent(),
            Statement.RETURN_GENERATED_KEYS)) {
         stmt.executeUpdate();
         try (ResultSet keys = stmt.getGeneratedKeys()) {
            keys.next();
            uid = UUID.fromString(keys.getString(2));
         }
         return uid;
      } catch (JsonProcessingException | SQLException e) {
         I_LOGGER.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   enum NodeType {GOLDEN_RECORD, ENCOUNTER, SOURCE_ID}

}
