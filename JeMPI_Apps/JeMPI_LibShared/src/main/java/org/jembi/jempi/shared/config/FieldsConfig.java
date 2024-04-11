package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FieldsConfig {

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

      auxGoldenRecordFields = jsonConfig.uniqueGoldenRecordFields()
                                        .stream()
                                        .map(field -> new AuxField(field.fieldName(), field.fieldType()))
                                        .toList();

      demographicFields = jsonConfig.demographicFields()
                                    .stream()
                                    .map(field -> new DemographicField(field.fieldName()))
                                    .collect(Collectors.toCollection(ArrayList::new));
   }

   public record NodeField(String name) {
   }

   public record NodeFields(List<NodeField> nodeFields) {
   }

   public record AdditionalNode(
         String name,
         NodeFields nodeFields) {
   }

   public record AuxField(String name,
                          String type) {
   }

   public record DemographicField(String name) {
   }


}
