package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AuxGoldenRecordField;
import org.jembi.jempi.shared.config.input.AuxInteractionField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class QueryGetExpandedGoldenRecords {

   private QueryGetExpandedGoldenRecords() {
   }

   private static String formattedUniqueInteractionField(final AuxInteractionField field) {
      return String.format(Locale.ROOT, "         Interaction.%s", field.scFieldName());
   }

   private static String formattedUniqueGoldenRecordField(final AuxGoldenRecordField field) {
      return String.format(Locale.ROOT, "      GoldenRecord.%s", field.scFieldName());
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
      final var goldenRecordDemographicFields = jsonConfig.demographicFields()
                                                          .stream()
                                                          .map(demographicField -> "      GoldenRecord.%s".formatted(
                                                                demographicField.scFieldName()))
                                                          .collect(Collectors.joining(System.lineSeparator()));
      final var interactionDemographicFields = jsonConfig.demographicFields()
                                                         .stream()
                                                         .map(demographicField -> "         Interaction.%s".formatted(
                                                               demographicField.scFieldName()))
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
             + jsonConfig.auxGoldenRecordFields()
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
             + jsonConfig.auxInteractionFields()
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
