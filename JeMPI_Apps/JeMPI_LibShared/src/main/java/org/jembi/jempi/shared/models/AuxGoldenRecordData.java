package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jembi.jempi.shared.config.DGraphConfig;
import org.jembi.jempi.shared.config.FieldsConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
           FIELDS_CONFIG.userAuxGoldenRecordFields
                 .stream()
                 .map(auxGoldenRecordUserField -> new AuxGoldenRecordUserField(
                       auxGoldenRecordUserField.ccName(),
                       (auxGoldenRecordUserField.source().interactionField() != null)
                             ? auxInteractionData.auxUserFields().stream()
                                                 .filter(auxInteractionUserField ->
                                                               auxInteractionUserField.scTag()
                                                                                      .equals(
                                                                                            auxGoldenRecordUserField.source()
                                                                                                                    .interactionField()))
                                                 .toList()
                                                 .getFirst().value()
                             : auxGoldenRecordUserField.source().generate() != null
                                   ? (auxGoldenRecordUserField.source().generate().interactionField() != null
                                         ? AppUtils.defaultIfFalsy(auxInteractionData.auxUserFields().stream()
                                                             .filter(auxInteractionUserField ->
                                                                   auxInteractionUserField.scTag()
                                                                                          .equals(
                                                                                                auxGoldenRecordUserField.source()
                                                                                                                        .generate()
                                                                                                                        .interactionField()))
                                                             .toList()
                                                             .getFirst().value(), AppUtils.applyFunction(auxGoldenRecordUserField.source().generate().func()))
                                         : AppUtils.applyFunction(auxGoldenRecordUserField.source().generate().func()))
                                   : null))
                 .toList());
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

   public static AuxGoldenRecordData fromCustomAuxGoldenRecordData(final JsonNode node) {
      final var dt = node.get(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED).textValue();
      final var d = Instant.parse(dt).atOffset(ZoneOffset.UTC).toLocalDateTime();
      return new AuxGoldenRecordData(
            d,
            node.get(DGraphConfig.PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED).booleanValue(),
            FIELDS_CONFIG.userAuxGoldenRecordFields
                  .stream()
                  .map(auxField -> new AuxGoldenRecordUserField(
                        auxField.ccName(),
                        node.get("GoldenRecord.%s".formatted(auxField.scName())).textValue()))
                  .toList());
   }

   public record AuxGoldenRecordUserField(
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }


}
