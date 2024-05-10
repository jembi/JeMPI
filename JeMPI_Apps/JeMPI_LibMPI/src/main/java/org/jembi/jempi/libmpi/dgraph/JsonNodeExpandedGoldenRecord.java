package org.jembi.jempi.libmpi.dgraph;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.models.ExpandedGoldenRecord;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeExpandedGoldenRecord(JsonNode jsonNode) {

   JsonNodeExpandedGoldenRecord(final String json) throws JsonProcessingException {
      this(OBJECT_MAPPER.readTree(json));
   }

   static ExpandedGoldenRecord toExpandedGoldenRecord(final JsonNode jsonNode) {
      final var goldenRecord = new JsonNodeGoldenRecord(jsonNode).toGoldenRecord();
      final var interactionsNode = jsonNode.get("GoldenRecord.interactions");
      final var interactions = new JsonNodeInteractions(interactionsNode).toInteractionsWithScore();
      return new ExpandedGoldenRecord(goldenRecord, interactions);
   }

   ExpandedGoldenRecord toExpandedGoldenRecord() {
      return toExpandedGoldenRecord(jsonNode);
   }

}


