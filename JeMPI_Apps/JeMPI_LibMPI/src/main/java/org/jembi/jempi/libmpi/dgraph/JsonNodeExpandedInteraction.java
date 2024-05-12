package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ExpandedInteraction;
import org.jembi.jempi.shared.models.GoldenRecordWithScore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeExpandedInteraction(JsonNode node) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeExpandedGoldenRecord.class);

   JsonNodeExpandedInteraction(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json).get("all");
   }

   ExpandedInteraction toExpandedInteraction() {
      final var interaction = new JsonNodeInteraction(node).toInteraction();
      final var goldenRecordsNode = node.get("~GoldenRecord.interactions");
      final List<GoldenRecordWithScore> goldenRecordsWithScores = new ArrayList<>();
      final Iterator<JsonNode> iter = goldenRecordsNode.elements();
      while (iter.hasNext()) {
         final var next = iter.next();
         goldenRecordsWithScores.add(new GoldenRecordWithScore(new JsonNodeGoldenRecord(next).toGoldenRecord(),
                                                               next.get("~GoldenRecord.interactions|score").floatValue()));
      }
      return new ExpandedInteraction(interaction, goldenRecordsWithScores);
   }


}
