package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueGoldenRecordField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class GoldenRecordFieldNames {

   private GoldenRecordFieldNames() {
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
      return "GoldenRecord." + toSnakeCase(additionalNode.nodeName()) + " {"
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
                         .map(GoldenRecordFieldNames::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueGoldenRecordFields()
                         .stream()
                         .map(GoldenRecordFieldNames::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(GoldenRecordFieldNames::formattedGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator();
   }

}
