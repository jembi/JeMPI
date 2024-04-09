package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueGoldenRecordField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class ExpandedGoldenRecordFieldNames {

   private ExpandedGoldenRecordFieldNames() {
   }

   private static String formattedGoldenRecordField(final DemographicField field) {
      return String.format(Locale.ROOT, "GoldenRecord.%s", field.fieldName());
   }

   private static String formattedUniqueGoldenRecordField(final UniqueGoldenRecordField field) {
      return String.format(Locale.ROOT, "GoldenRecord.%s", field.fieldName());
   }

   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedAdditionalNodes(final AdditionalNode additionalNode) {
      return "   Interaction." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "      uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "      " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "   }";
   }

   public static String create(final JsonConfig jsonConfig) {
      return GoldenRecordFieldNames.create(jsonConfig)
             + "GoldenRecord.interactions @facets(score) {"
             + System.lineSeparator()
             + "   uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(ExpandedGoldenRecordFieldNames::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(field -> "   Interaction." + field.fieldName())
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(field -> "   Interaction." + field.fieldName())
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
