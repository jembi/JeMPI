package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueInteractionField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class QueryGetInteractionByUid {

   private QueryGetInteractionByUid() {
   }

   private static String formattedInteractionField(final DemographicField field) {
      return String.format(Locale.ROOT, "      Interaction.%s", field.fieldName());
   }

   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      return String.format(Locale.ROOT, "      Interaction.%s", field.fieldName());
   }

   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedAdditionalNodes(final AdditionalNode additionalNode) {
      return "      Interaction." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "        uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "        " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "      }";
   }

   public static String create(final JsonConfig jsonConfig) {
      return "query interactionByUid($uid: string) {"
             + System.lineSeparator()
             + "   all(func: uid($uid)) {"
             + System.lineSeparator()
             + "      uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetInteractionByUid::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(QueryGetInteractionByUid::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(QueryGetInteractionByUid::formattedInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "   }"
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
