package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ExpandedGoldenRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonNodeExpandedGoldenRecords(JsonNode all) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeExpandedGoldenRecords.class);

   JsonNodeExpandedGoldenRecords(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      LOGGER.debug("{}", System.lineSeparator() + json);
      return OBJECT_MAPPER.readTree(json).get("all");
   }

   List<ExpandedGoldenRecord> toExpandedGoldenRecordList() {
      final List<ExpandedGoldenRecord> expandedGoldenRecords = new ArrayList<>();
      final Iterator<JsonNode> iter = all.elements();
      while (iter.hasNext()) {
         final var next = iter.next();
         expandedGoldenRecords.add(JsonNodeExpandedGoldenRecord.toExpandedGoldenRecord(next));
      }
      try {
         LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(expandedGoldenRecords));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return expandedGoldenRecords;
   }

}
