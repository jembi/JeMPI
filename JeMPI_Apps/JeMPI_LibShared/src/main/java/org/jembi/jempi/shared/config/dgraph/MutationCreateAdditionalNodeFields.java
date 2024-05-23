package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNodeField;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateAdditionalNodeFields {

   private MutationCreateAdditionalNodeFields() {
   }

   private static String formattedAdditionalNodeField(
         final String nodeName,
         final List<AdditionalNodeField> fields) {
      return fields.stream()
                   .map(field -> String.format(Locale.ROOT,
                                               "%-39s%-10s%-35s.",
                                               nodeName + "." + field.fieldName() + ":",
                                               field.fieldType().toLowerCase(),
                                               "@index(exact)"))
                   .collect(Collectors.joining(System.lineSeparator()));
   }

   public static String create(final JsonConfig jsonConfig) {
      return jsonConfig.additionalNodes()
                       .stream()
                       .map(node -> formattedAdditionalNodeField(node.nodeName(), node.fields()))
                       .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator();
   }

}
