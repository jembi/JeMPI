package org.jembi.jempi.shared.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FieldsConfig {

   private static final Logger LOGGER = LogManager.getLogger(FieldsConfig.class);

   public final List<AuxField> auxInteractionFields;
   public final List<AuxField> auxGoldenRecordFields;
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
                                        .map(field -> new AuxField(field.fieldName(), field.fieldType()))
                                        .toList();
      auxInteractionFields = jsonConfig.auxInteractionFields()
                                       .stream()
                                       .map(field -> new AuxField(field.fieldName(), field.fieldType()))
                                       .toList();

      demographicFields = jsonConfig.demographicFields()
                                    .stream()
                                    .map(field -> new DemographicField(field.fieldName(),
                                                                       AppUtils.snakeToCamelCase(field.fieldName())))
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
         String name,
         String type) {
   }

   public record DemographicField(
         // snake case
         String scName,
         // camel case
         String ccName) {
   }


}
