package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class QueryGetExpandedGoldenRecords {

   private QueryGetExpandedGoldenRecords() {
   }

   private static String formattedInteractionField(final int idx) {
      return String.format(Locale.ROOT, "         Interaction.demographic_field_%02d", idx);
   }

   private static String formattedGoldenRecordField(final int idx) {
      return String.format(Locale.ROOT, "      GoldenRecord.demographic_field_%02d", idx);
   }


   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      return String.format(Locale.ROOT, "         Interaction.%s", field.fieldName());
   }

   private static String formattedUniqueGoldenRecordField(final UniqueGoldenRecordField field) {
      return String.format(Locale.ROOT, "      GoldenRecord.%s", field.fieldName());
   }

   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedInteractionAdditionalNodes(final AdditionalNode additionalNode) {
      return "         Interaction." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "           uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "           " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "         }";
   }

   private static String formattedGoldenRecordAdditionalNodes(final AdditionalNode additionalNode) {
      return "      GoldenRecord." + toSnakeCase(additionalNode.nodeName()) + " {"
             + System.lineSeparator()
             + "         uid"
             + System.lineSeparator()
             + additionalNode.fields()
                             .stream()
                             .map(node -> "         " + additionalNode.nodeName() + "." + node.fieldName())
                             .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "      }";
   }

   public static String create(final JsonConfig jsonConfig) {
      final var goldenRecordDemographicFields = IntStream.range(0, jsonConfig.demographicFields().size())
                                             .mapToObj(QueryGetExpandedGoldenRecords::formattedGoldenRecordField)
                                             .collect(Collectors.joining(System.lineSeparator()));
      final var interactionDemographicFields = IntStream.range(0, jsonConfig.demographicFields().size())
                                                         .mapToObj(QueryGetExpandedGoldenRecords::formattedInteractionField)
                                                         .collect(Collectors.joining(System.lineSeparator()));
      return "query expandedGoldenRecord() {"
             + System.lineSeparator()
             + "   all(func: uid(%s), orderdesc: GoldenRecord.aux_date_created) {"
             + System.lineSeparator()
             + "      uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetExpandedGoldenRecords::formattedGoldenRecordAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueGoldenRecordFields()
                         .stream()
                         .map(QueryGetExpandedGoldenRecords::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + goldenRecordDemographicFields
             + System.lineSeparator()
             + "      GoldenRecord.interactions @facets(score) {"
             + System.lineSeparator()
             + "         uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetExpandedGoldenRecords::formattedInteractionAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(QueryGetExpandedGoldenRecords::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + interactionDemographicFields
             + System.lineSeparator()
             + "      }"
             + System.lineSeparator()
             + "   }"
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
