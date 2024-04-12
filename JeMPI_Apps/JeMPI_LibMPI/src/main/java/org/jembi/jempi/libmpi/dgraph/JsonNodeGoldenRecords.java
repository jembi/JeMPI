package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeGoldenRecords(JsonNode all) {

   JsonNodeGoldenRecords(final String json) throws JsonProcessingException {
      this(OBJECT_MAPPER.readTree(json).get("all"));
   }

   List<GoldenRecord> toGoldenRecordList() {
      final List<GoldenRecord> goldenRecords = new ArrayList<>();
      final Iterator<JsonNode> iter = all.elements();
      while (iter.hasNext()) {
         final var next = iter.next();
         goldenRecords.add(JsonNodeGoldenRecord.toGoldenRecord(next));
      }
      return goldenRecords;
   }

}
