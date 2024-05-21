package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AuxGoldenRecordField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateGoldenRecordType {

   private MutationCreateGoldenRecordType() {
   }

   private static String formattedUniqueGoldenRecordField(final AuxGoldenRecordField field) {
      final var name = field.scFieldName();
      return String.format(Locale.ROOT, "   GoldenRecord.%s", name);
   }

   private static String formattedAdditionalGoldenRecordNode(final AdditionalNode additionalNode) {
      final var snake = AppUtils.camelToSnake(additionalNode.nodeName()) + ":";
      final var name = snake.startsWith("_")
            ? snake.substring(1)
            : snake;
      return String.format(Locale.ROOT, "   GoldenRecord.%-39s%s", name, "[" + additionalNode.nodeName() + "]");
   }

   public static String create(final JsonConfig jsonConfig) {
      final var demographicFields = jsonConfig.demographicFields()
                                              .stream()
                                              .map(demographicField -> "   GoldenRecord.%s".formatted(demographicField.scFieldName()))
                                              .collect(Collectors.joining(System.lineSeparator()));
      return "type GoldenRecord {"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(MutationCreateGoldenRecordType::formattedAdditionalGoldenRecordNode)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxGoldenRecordFields()
                         .stream()
                         .map(MutationCreateGoldenRecordType::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
             + System.lineSeparator()
             + String.format(Locale.ROOT, "   GoldenRecord.%-39s%s", "interactions:", "[Interaction]")
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
