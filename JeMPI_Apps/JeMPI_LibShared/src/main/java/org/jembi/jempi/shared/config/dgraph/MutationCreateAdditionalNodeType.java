package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AdditionalNodeField;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateAdditionalNodeType {

   private MutationCreateAdditionalNodeType() {
   }

   private static String formattedAdditionalNodeField(
         final String nodeName,
         final List<AdditionalNodeField> fields) {
      return fields.stream()
                   .map(field -> String.format(Locale.ROOT,
                                               "   %s",
                                               nodeName + "." + field.fieldName()))
                   .collect(Collectors.joining(System.lineSeparator()));
   }

   public static String formattedNodeType(final AdditionalNode additionalNode) {
      return String.format("type %s {", additionalNode.nodeName())
             + System.lineSeparator()
             + formattedAdditionalNodeField(additionalNode.nodeName(), additionalNode.fields())
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

   public static String create(final JsonConfig jsonConfig) {
      return jsonConfig.additionalNodes()
                       .stream()
                       .map(MutationCreateAdditionalNodeType::formattedNodeType)
                       .collect(Collectors.joining(System.lineSeparator()));
   }

}
