package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class InteractionFieldNames {

   private InteractionFieldNames() {
   }

   private static String formattedInteractionField(final DemographicField field) {
      return String.format(Locale.ROOT, "Interaction.%s", field.fieldName());
   }

   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      return String.format(Locale.ROOT, "Interaction.%s", field.fieldName());
   }

   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedAdditionalNodes(final AdditionalNode additionalNode) {
      return "Interaction." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "   uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "   " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "}";
   }

   public static String create(final JsonConfig jsonConfig) {
      return "uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(InteractionFieldNames::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(InteractionFieldNames::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(InteractionFieldNames::formattedInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator();
   }

}
