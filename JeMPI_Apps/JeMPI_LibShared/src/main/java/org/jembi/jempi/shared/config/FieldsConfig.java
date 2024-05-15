package org.jembi.jempi.shared.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.Source;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FieldsConfig {
   public static final String AUX_INTERACTION_DATE_CREATED_FIELD_NAME_CC = "auxDateCreated";
   public static final String AUX_INTERACTION_DATE_CREATED_FIELD_NAME_SC = "aux_date_created";
   public static final String OPTIONAL_INTERACTION_AUX_ID_FIELD_NAME_CC = "auxId";
   public static final String OPTIONAL_AUX_INTERACTION_AUX_ID_FIELD_NAME_SC = "aux_id";

   public static final String DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_FIELD_NAME_CC = "auxClinicalData";
   public static final int DEPRECATED_INTERACTION_AUX_CLINICAL_DATA_IDX = 1;

   public static final String AUX_GOLDEN_RECORD_DATE_CREATED_FIELD_NAME_CC = "auxDateCreated";
   public static final String AUX_GOLDEN_RECORD_DATE_CREATED_FIELD_NAME_SC = "aux_date_created";
   public static final String AUX_GOLDEN_RECORD_UPDATE_ENABLED_FIELD_NAME_CC = "auxAutoUpdateEnabled";
   public static final String AUX_GOLDEN_RECORD_UPDATE_ENABLED_FIELD_NAME_SC = "aux_auto_update_enabled";

   public static final String OPTIONAL_AUX_GOLDEN_RECORD_AUX_ID_FIELD_NAME_CC = "auxId";
   public static final String OPTIONAL_AUX_GOLDEN_RECORD_AUX_ID_FIELD_NAME_SC = "aux_id";

   private static final Logger LOGGER = LogManager.getLogger(FieldsConfig.class);

   public final Integer optionalAuxGoldenRecordAuxIdIdx;
   public final Integer optionalAuxInteractionAuxIdIdx;
   public final List<AuxField> auxInteractionFields;
   public final List<AuxField> userAuxInteractionFields;
   public final List<AuxField> auxGoldenRecordFields;
   public final List<AuxField> userAuxGoldenRecordFields;
   public final List<DemographicField> demographicFields;
   public final List<AdditionalNode> additionalNodes;

   public FieldsConfig(final JsonConfig jsonConfig) {
      additionalNodes = jsonConfig.additionalNodes()
                                  .stream()
                                  .map(node -> new AdditionalNode(node.nodeName(),
                                                                  new NodeFields(node.fields()
                                                                                     .stream()
                                                                                     .map(nodeField -> new NodeField(nodeField.fieldName()))
                                                                                     .toList())))
                                  .collect(Collectors.toCollection(ArrayList::new));

      auxGoldenRecordFields = jsonConfig.auxGoldenRecordFields()
                                        .stream()
                                        .map(field -> new AuxField(field.scFieldName(),
                                                                   AppUtils.snakeToCamelCase(field.scFieldName()),
                                                                   field.fieldType(),
                                                                   field.source()))
                                        .toList();
      userAuxGoldenRecordFields = auxGoldenRecordFields
            .stream()
            .filter(f -> !(f.scName.equals(AUX_GOLDEN_RECORD_DATE_CREATED_FIELD_NAME_SC)
                           || f.scName.equals(AUX_GOLDEN_RECORD_UPDATE_ENABLED_FIELD_NAME_SC)))
            .toList();
      final Integer[] auxGoldenRecordAuxIdIdx = new Integer[]{null};
      IntStream.range(0, userAuxGoldenRecordFields.size())
               .forEach(i -> {
                           if (userAuxGoldenRecordFields.get(i).scName
                                 .equals(OPTIONAL_AUX_GOLDEN_RECORD_AUX_ID_FIELD_NAME_SC)) {
                              auxGoldenRecordAuxIdIdx[0] = i;
                           }
                        }
                       );
      optionalAuxGoldenRecordAuxIdIdx = auxGoldenRecordAuxIdIdx[0];

      auxInteractionFields = jsonConfig.auxInteractionFields()
                                       .stream()
                                       .map(field -> new AuxField(field.scFieldName(),
                                                                  AppUtils.snakeToCamelCase(field.scFieldName()),
                                                                  field.fieldType(),
                                                                  field.source()))
                                       .toList();
      userAuxInteractionFields = auxInteractionFields
            .stream()
            .filter(f -> !(f.scName.equals(AUX_INTERACTION_DATE_CREATED_FIELD_NAME_SC)))
            .toList();
      final Integer[] auxInteractionAuxIdIdx = new Integer[]{null};
      IntStream.range(0, userAuxInteractionFields.size())
               .forEach(i -> {
                           if (userAuxInteractionFields.get(i).scName
                                 .equals(OPTIONAL_AUX_INTERACTION_AUX_ID_FIELD_NAME_SC)) {
                              auxInteractionAuxIdIdx[0] = i;
                           }
                        }
                       );
      optionalAuxInteractionAuxIdIdx = auxInteractionAuxIdIdx[0];


      demographicFields = jsonConfig.demographicFields()
                                    .stream()
                                    .map(field -> new DemographicField(field.scFieldName(),
                                                                       AppUtils.snakeToCamelCase(field.scFieldName())))
                                    .collect(Collectors.toCollection(ArrayList::new));
   }

   public int findIndexOfDemographicField(final String fieldName) {
      for (int i = 0; i < demographicFields.size(); i++) {
         if (demographicFields.get(i).ccName.equals(fieldName)) {
            return i;
         }
      }
      LOGGER.error("invalid fieldName: {} ", fieldName);
      return -1;
   }

   public record NodeField(String name) {
   }

   public record NodeFields(List<NodeField> nodeFields) {
   }

   public record AdditionalNode(
         String name,
         NodeFields nodeFields) {
   }

   public record AuxField(
         String scName,
         String ccName,
         String type,
         Source source) {
   }

   public record DemographicField(
         // snake case
         String scName,
         // camel case
         String ccName) {
   }


}
