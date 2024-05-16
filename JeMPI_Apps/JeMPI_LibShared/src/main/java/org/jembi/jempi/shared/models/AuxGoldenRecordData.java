package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.config.FieldsConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.FIELDS_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuxGoldenRecordData(
      java.time.LocalDateTime auxDateCreated,
      Boolean auxAutoUpdateEnabled,
      List<AuxGoldenRecordUserField> auxUserFields) {

   public AuxGoldenRecordData(final AuxInteractionData auxInteractionData) {
      this(LocalDateTime.now(),
           true,
           List.of(deprecatedGetFieldAuxId(auxInteractionData.auxUserFields()
                                                             .get(FIELDS_CONFIG.optionalInteractionAuxIdIdx)
                                                             .value()))
          );
   }

   public static JsonNode fromAuxGoldenRecordData(final AuxGoldenRecordData auxGoldenRecordData) {
      final var objectNode = OBJECT_MAPPER.createObjectNode();
      objectNode.put(FieldsConfig.GOLDEN_RECORD_AUX_DATE_CREATED_FIELD_NAME_CC, auxGoldenRecordData.auxDateCreated.toString());
      objectNode.put(FieldsConfig.GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED_FIELD_NAME_CC,
                     auxGoldenRecordData.auxAutoUpdateEnabled.booleanValue());
      auxGoldenRecordData.auxUserFields.forEach(field -> {
         if (field.ccTag != null && !field.ccTag.isEmpty()) {
            objectNode.put(field.ccTag, field.value);
         }
      });
      return objectNode;
   }

   public static AuxGoldenRecordUserField deprecatedGetFieldAuxId(final String value) {
      return new AuxGoldenRecordUserField(FieldsConfig.OPTIONAL_GOLDEN_RECORD_AUX_ID_FIELD_NAME_CC, value);
   }

   public record AuxGoldenRecordUserField(
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }


}
