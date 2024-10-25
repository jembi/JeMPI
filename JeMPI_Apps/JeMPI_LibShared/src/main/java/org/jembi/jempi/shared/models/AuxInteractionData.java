package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

   private static final Logger LOGGER = LogManager.getLogger(AuxInteractionData.class);         

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
      final var dt = node.get(FieldsConfig.INTERACTION_AUX_DATE_CREATED_FIELD_NAME_CC).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      LOGGER.debug("{}", dt);
      for (int i = 0; i < FIELDS_CONFIG.userAuxInteractionFields.size(); i++) {
            LOGGER.debug("{} {}", FIELDS_CONFIG.userAuxInteractionFields.get(i).ccName(),
                                          FIELDS_CONFIG.userAuxInteractionFields.get(i).scName());
      }
      return new AuxInteractionData(
            d,
            FIELDS_CONFIG.userAuxInteractionFields
                  .stream()
                  .map(auxField -> new AuxInteractionUserField(
                        auxField.scName(),
                        auxField.ccName(),
                        node.get(auxField.ccName()) != null
                              ? node.get(auxField.ccName()).textValue()
                              : ""))
                  .toList());
   }

   public static AuxInteractionData fromAuxInteractionData(final JsonNode node) {
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
