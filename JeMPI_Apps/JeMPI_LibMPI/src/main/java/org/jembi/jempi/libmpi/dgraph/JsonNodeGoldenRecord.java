package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.DGraphConfig;
import org.jembi.jempi.shared.models.CustomSourceId;
import org.jembi.jempi.shared.models.AuxGoldenRecordData;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.Config.JSON_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeGoldenRecord(JsonNode jsonNode) {

   private static final Logger LOGGER = LogManager.getLogger(JsonNodeGoldenRecord.class);

   JsonNodeGoldenRecord(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      return OBJECT_MAPPER.readTree(json).get("all");
   }

   GoldenRecord toGoldenRecord() {
      final var sourceIdList = new ArrayList<CustomSourceId>();
      final var sourceIdNode = jsonNode.get("GoldenRecord.source_id");
      if (!(sourceIdNode == null || sourceIdNode.isNull() || sourceIdNode.isMissingNode())) {
         final var iter = sourceIdNode.elements();
         while (iter.hasNext()) {
            final var element = iter.next();
            final var uid = element.get("uid").textValue();
            final var f = element.get("SourceId.facility");
            final var p = element.get("SourceId.patient");
            final var facility = (!(f == null || f.isMissingNode()))
                  ? f.textValue()
                  : null;
            final var patient = (!(f == null || p.isMissingNode()))
                  ? f.textValue()
                  : null;
            sourceIdList.add(new CustomSourceId(uid, facility, patient));
         }
      }
      final var dt = jsonNode.get(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      final var b = jsonNode.get(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED).booleanValue();
      final var t = jsonNode.get(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_ID).textValue();
      final var customUniqueGoldenRecordData = new AuxGoldenRecordData(d, b, t);
      final var demographicData =
            new DemographicData(IntStream.range(0, JSON_CONFIG.demographicFields().size()).mapToObj(idx -> {
               final var fieldName = JSON_CONFIG.demographicFields().get(idx).fieldName();
               final var v = jsonNode.get("GoldenRecord.demographic_field_%02d".formatted(idx));
               return (!(v == null || v.isMissingNode()))
                     ? new DemographicData.DemographicField(AppUtils.snakeToCamelCase(fieldName), v.textValue())
                     : null;
            }).toList());
      return new GoldenRecord(jsonNode.get("uid").textValue(), sourceIdList, customUniqueGoldenRecordData, demographicData);
   }

}

