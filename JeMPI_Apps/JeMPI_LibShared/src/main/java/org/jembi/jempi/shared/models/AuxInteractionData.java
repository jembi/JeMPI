package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.config.DGraphConfig;
import org.jembi.jempi.shared.config.FieldsConfig;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.FIELDS_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuxInteractionData(
      java.time.LocalDateTime auxDateCreated,
      List<AuxInteractionUserField> auxUserFields) {

   public static JsonNode fromAuxInteractionData(final AuxInteractionData auxInteractionData) {
      final var objectNode = OBJECT_MAPPER.createObjectNode();
      objectNode.put(FieldsConfig.INTERACTION_AUX_DATE_CREATED_FIELD_NAME_CC, auxInteractionData.auxDateCreated.toString());
      auxInteractionData.auxUserFields.forEach(field -> {
         if (field.ccTag != null && !field.ccTag.isEmpty()) {
            objectNode.put(field.ccTag, field.value);
         }
      });
      return objectNode;
   }

   public static AuxInteractionData fromCustomAuxInteractionData(final JsonNode node) {
      final var dt = node.get(DGraphConfig.PREDICATE_INTERACTION_AUX_DATE_CREATED).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      return new AuxInteractionData(
            d,
            FIELDS_CONFIG.userAuxInteractionFields
                  .stream()
                  .map(auxField -> new AuxInteractionUserField(
                        auxField.scName(),
                        auxField.ccName(),
                        node.get("Interaction.%s".formatted(auxField.scName())).textValue()))
                  .toList());
   }

   public record AuxInteractionUserField(
         @JsonProperty("scTag") String scTag,
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }

}
