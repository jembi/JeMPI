package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueGoldenRecordField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateGoldenRecordType {

   private MutationCreateGoldenRecordType() {
   }

   private static String formattedGoldenRecordField(final int idx) {
      return String.format(Locale.ROOT, "   GoldenRecord.demographic_field_%02d", idx);
   }

   private static String formattedUniqueGoldenRecordField(final UniqueGoldenRecordField field) {
      final var name = field.fieldName();
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
      final var demoGraphicFields = IntStream.range(0, jsonConfig.demographicFields().size())
                                             .mapToObj(MutationCreateGoldenRecordType::formattedGoldenRecordField)
                                             .collect(Collectors.joining(System.lineSeparator()));
      return "type GoldenRecord {"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(MutationCreateGoldenRecordType::formattedAdditionalGoldenRecordNode)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueGoldenRecordFields()
                         .stream()
                         .map(MutationCreateGoldenRecordType::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demoGraphicFields
//             + jsonConfig.demographicFields()
//                         .stream()
//                         .map(MutationCreateGoldenRecordType::formattedGoldenRecordField)
//                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + String.format(Locale.ROOT, "   GoldenRecord.%-39s%s", "interactions:", "[Interaction]")
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
