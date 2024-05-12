package org.jembi.jempi.libmpi.dgraph;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ExpandedGoldenRecord;
import org.jembi.jempi.shared.models.InteractionWithScore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeExpandedGoldenRecord(JsonNode node) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeExpandedGoldenRecord.class);

   JsonNodeExpandedGoldenRecord(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json).get("all");
   }

   ExpandedGoldenRecord toExpandedGoldenRecord() {
      final var goldenRecord = new JsonNodeGoldenRecord(node).toGoldenRecord();
      final var interactionsNode = node.get("GoldenRecord.interactions");
      final List<InteractionWithScore> interactionsWithScores = new ArrayList<>();
      final Iterator<JsonNode> iter = interactionsNode.elements();
      while (iter.hasNext()) {
         final var next = iter.next();
         interactionsWithScores.add(new JsonNodeInteraction(next).toInteractionWithScore());
      }
      return new ExpandedGoldenRecord(goldenRecord, interactionsWithScores);
   }

}


