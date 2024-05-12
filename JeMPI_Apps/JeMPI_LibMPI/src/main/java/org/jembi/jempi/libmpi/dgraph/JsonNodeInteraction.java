package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.DGraphConfig;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.Config.JSON_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
record JsonNodeInteraction(JsonNode node) {
   private static final Logger LOGGER = LogManager.getLogger(JsonNodeInteraction.class);

   JsonNodeInteraction(final String json) throws JsonProcessingException {
      this(toJsonNode(json));
   }

   private static JsonNode toJsonNode(final String json) throws JsonProcessingException {
      LOGGER.debug("{}", json);
      return OBJECT_MAPPER.readTree(json);
   }

   Interaction toInteraction() {
      final var sourceIdNode = node.get("Interaction.source_id");
      final var facilityNode = sourceIdNode.get("SourceId.facility");
      final var patientNode = sourceIdNode.get("SourceId.patient");

      final var sourceId = new CustomSourceId(sourceIdNode.get("uid").textValue(),
                                              (!(facilityNode == null || facilityNode.isMissingNode()))
                                                    ? facilityNode.textValue()
                                                    : null,
                                              (!(patientNode == null || patientNode.isMissingNode()))
                                                    ? patientNode.textValue()
                                                    : null);

      final var dt = node.get(DGraphConfig.PREDICATE_INTERACTION_AUX_DATE_CREATED).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      final var i = node.get(DGraphConfig.PREDICATE_INTERACTION_AUX_ID).textValue();
      final var c = node.get(DGraphConfig.PREDICATE_INTERACTION_AUX_CLINICAL_DATA).textValue();
      final var auxInteractionData = new AuxInteractionData(d, i, c);
      final var demographicData =
            new DemographicData(IntStream.range(0, JSON_CONFIG.demographicFields().size()).mapToObj(idx -> {
               final var fieldName = JSON_CONFIG.demographicFields().get(idx).fieldName();
               final var v = node.get("Interaction.demographic_field_%02d".formatted(idx));
               return (!(v == null || v.isMissingNode()))
                     ? new DemographicData.DemographicField(AppUtils.snakeToCamelCase(fieldName), v.textValue())
                     : null;
            }).toList());
      return new Interaction(node.get("uid").textValue(), sourceId, auxInteractionData, demographicData);
   }

   InteractionWithScore toInteractionWithScore() {
      final var interaction = toInteraction();
      final var score = node.get("GoldenRecord.interactions|score").floatValue();
      return new InteractionWithScore(interaction, score);
   }


}
