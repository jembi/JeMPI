package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.stream.Collectors;

public final class GoldenRecordFieldNames {

   private GoldenRecordFieldNames() {
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
      final var demographicFields = jsonConfig.demographicFields()
                                              .stream()
                                              .map(demographicField -> "GoldenRecord.%s".formatted(demographicField.scFieldName()))
                                              .collect(Collectors.joining(System.lineSeparator()));
      return "uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(GoldenRecordFieldNames::formattedAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxGoldenRecordFields()
                         .stream()
                         .map(f -> "GoldenRecord.%s".formatted(f.scFieldName()))
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
             + System.lineSeparator();
   }

}
