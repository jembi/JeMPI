package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.config.FieldsConfig;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuxInteractionData(
      java.time.LocalDateTime auxDateCreated,
      List<AuxInteractionUserField> auxUserFields) {

   public static JsonNode fromAuxInteractionData(final AuxInteractionData auxInteractionData) {
      final var objectNode = OBJECT_MAPPER.createObjectNode();
      objectNode.put(FieldsConfig.AUX_INTERACTION_DATE_CREATED_FIELD_NAME_CC, auxInteractionData.auxDateCreated.toString());
      auxInteractionData.auxUserFields.forEach(field -> {
         if (field.ccTag != null && !field.ccTag.isEmpty()) {
            objectNode.put(field.ccTag, field.value);
         }
      });
      return objectNode;
   }

   public static AuxInteractionData deprecatedFromCustomAuxInteractionData(final JsonNode jsonNode) {
      final var dt = jsonNode.get(FieldsConfig.AUX_INTERACTION_DATE_CREATED_FIELD_NAME_CC).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      return new AuxInteractionData(
            d,
            List.of(new AuxInteractionUserField(
                          FieldsConfig.DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC,
                          jsonNode.get(FieldsConfig.DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC).textValue()),
                    new AuxInteractionUserField(
                          FieldsConfig.DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC,
                          jsonNode.get(FieldsConfig.DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC).textValue())));
   }

   public static AuxInteractionUserField deprecatedGetFieldAuxId(final String value) {
      return new AuxInteractionUserField(FieldsConfig.OPTIONAL_INTERACTION_AUX_ID_FIELD_NAME_CC, value);
   }

   public static AuxInteractionUserField deprecatedGetFieldAuxClinicalData(final String value) {
      return new AuxInteractionUserField(FieldsConfig.DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC, value);
   }

   public record AuxInteractionUserField(
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }

}
