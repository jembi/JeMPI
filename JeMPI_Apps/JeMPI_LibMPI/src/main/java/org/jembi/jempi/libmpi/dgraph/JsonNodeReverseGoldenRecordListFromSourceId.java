package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.ExpandedSourceId;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonNodeReverseGoldenRecordListFromSourceId(JsonNode jsonNode) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeReverseGoldenRecordListFromSourceId.class);

   JsonNodeReverseGoldenRecordListFromSourceId(final String json) throws JsonProcessingException {
      this(OBJECT_MAPPER.readTree(json).get("all"));
   }

   public List<ExpandedSourceId> toExpandedSourceId() {
      final var elements = jsonNode.elements();
      final var list = new ArrayList<ExpandedSourceId>();
      while (elements.hasNext()) {
         final var jsonNode = elements.next();
         final var uid = jsonNode.get("uid").asText();
         final var facility = jsonNode.get("SourceId.facility").asText();
         final var patient = jsonNode.get("SourceId.patient").asText();
         final var iter = jsonNode.get("~GoldenRecord.source_id").elements();
         final var goldenRecords = new ArrayList<GoldenRecord>();
         while (iter.hasNext()) {
            final var jsonNode1 = iter.next();
            final var goldenRecord = JsonNodeGoldenRecord.toGoldenRecord(jsonNode1);
            goldenRecords.add(goldenRecord);
            final var result = new ExpandedSourceId(new CustomSourceId(uid, facility, patient), goldenRecords);
            list.add(result);
         }
      }
      return list;

   }

}
