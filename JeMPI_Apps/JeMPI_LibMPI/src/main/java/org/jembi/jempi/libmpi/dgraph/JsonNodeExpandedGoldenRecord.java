package org.jembi.jempi.libmpi.dgraph;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ExpandedGoldenRecord;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeExpandedGoldenRecord(JsonNode jsonNode) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeExpandedGoldenRecord.class);

   JsonNodeExpandedGoldenRecord(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json).get("all");
   }

   ExpandedGoldenRecord toExpandedGoldenRecord() {
      final var goldenRecord = new JsonNodeGoldenRecord(jsonNode).toGoldenRecord();
      final var interactionsNode = jsonNode.get("GoldenRecord.interactions");
      final var interactions = new JsonNodeInteractions(interactionsNode).toInteractionsWithScore();
      return new ExpandedGoldenRecord(goldenRecord, interactions);
   }

}


