package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.ExpandedInteraction;
import org.jembi.jempi.shared.models.LibMPIPagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeExpandedInteractions(JsonNode node) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeExpandedInteractions.class);

   JsonNodeExpandedInteractions(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json);
   }

   PaginatedResultSet<ExpandedInteraction> toExpandedInteractions() {
      final List<ExpandedInteraction> expandedInteractions = new ArrayList<>();
      final Iterator<JsonNode> iter = node.get("all").elements();
      while (iter.hasNext()) {
         final var next = iter.next();
         expandedInteractions.add(new JsonNodeExpandedInteraction(next).toExpandedInteraction());
      }
      final var paginationNode = node.get("pagination");
      if (paginationNode != null && !paginationNode.isMissingNode()) {
         try {
            final var pagination = Arrays.stream(OBJECT_MAPPER.treeToValue(paginationNode, LibMPIPagination[].class)).toList();
            return new PaginatedResultSet<>(expandedInteractions, pagination);
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
      return new PaginatedResultSet<>(expandedInteractions, List.of(new LibMPIPagination(expandedInteractions.size())));
   }

}
