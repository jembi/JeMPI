package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.AuxGoldenRecordData;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.SourceId;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;

import static org.jembi.jempi.shared.config.Config.JSON_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeGoldenRecord(JsonNode node) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeGoldenRecord.class);

   JsonNodeGoldenRecord(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json);
   }

   GoldenRecord toGoldenRecord() {
      if (node.isMissingNode()) {
         return null;
      }
      final var uidNode = node.get("uid");
      if (uidNode == null || uidNode.isNull() || uidNode.isMissingNode()) {
         return null;
      }
      final var sourceIdList = new ArrayList<SourceId>();
      final var sourceIdNode = node.get("GoldenRecord.source_id");
      if (!(sourceIdNode == null || sourceIdNode.isNull() || sourceIdNode.isMissingNode())) {
         final var iter = sourceIdNode.elements();
         while (iter.hasNext()) {
            final var element = iter.next();
            final var uid = element.get("uid").textValue();
            final var facilityNode = element.get("SourceId.facility");
            final var patientNode = element.get("SourceId.patient");
            final var facility = (!(facilityNode == null || facilityNode.isMissingNode()))
                  ? facilityNode.textValue()
                  : null;
            final var patient = (!(patientNode == null || patientNode.isMissingNode()))
                  ? patientNode.textValue()
                  : null;
            sourceIdList.add(new SourceId(uid, facility, patient));
         }
      }
      final var auxGoldenRecordData = AuxGoldenRecordData.fromCustomAuxGoldenRecordData(node);
      final var demographicData =
            new DemographicData(
                  JSON_CONFIG.demographicFields().stream().map(demographicField -> {
                     final var fieldName = demographicField.scFieldName();
                     final var v = node.get("GoldenRecord.%s".formatted(fieldName));
                     return (!(v == null || v.isMissingNode()))
                           ? new DemographicData.DemographicField(AppUtils.snakeToCamelCase(fieldName), v.textValue())
                           : null;
                  }).toList());
      return new GoldenRecord(uidNode.textValue(), sourceIdList, auxGoldenRecordData, demographicData);
   }

}

