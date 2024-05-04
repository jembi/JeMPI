package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.AuxGoldenRecordField;
import org.jembi.jempi.shared.config.input.AuxInteractionField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class QueryGetExpandedInteractions {

   private QueryGetExpandedInteractions() {
   }

   private static String formattedInteractionField(final int idx) {
      return String.format(Locale.ROOT, "      Interaction.demographic_field_%02d", idx);
   }

   private static String formattedGoldenRecordField(final int idx) {
      return String.format(Locale.ROOT, "         GoldenRecord.demographic_field_%02d", idx);
   }


   private static String formattedUniqueInteractionField(final AuxInteractionField field) {
      return String.format(Locale.ROOT, "      Interaction.%s", field.fieldName());
   }

   private static String formattedUniqueGoldenRecordField(final AuxGoldenRecordField field) {
      return String.format(Locale.ROOT, "         GoldenRecord.%s", field.fieldName());
   }


   private static String toSnakeCase(final String string) {
      final var s = AppUtils.camelToSnake(string);
      return s.startsWith("_")
            ? s.substring(1)
            : s;
   }

   private static String formattedInteractionAdditionalNodes(final AdditionalNode additionalNode) {
      return "      Interaction." + toSnakeCase(additionalNode.nodeName()) + " {"
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

   private static String formattedGoldenRecordAdditionalNodes(final AdditionalNode additionalNode) {
      return "         GoldenRecord." + toSnakeCase(additionalNode.nodeName()) + " {"
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


   public static String create(final JsonConfig jsonConfig) {
      final var goldenRecordDemographicFields = IntStream
            .range(0, jsonConfig.demographicFields().size())
            .mapToObj(QueryGetExpandedInteractions::formattedGoldenRecordField)
            .collect(Collectors.joining(System.lineSeparator()));
      final var interactionDemographicFields = IntStream
            .range(0, jsonConfig.demographicFields().size())
            .mapToObj(QueryGetExpandedInteractions::formattedInteractionField)
            .collect(Collectors.joining(System.lineSeparator()));
      return "query expandedInteraction() {"
             + System.lineSeparator()
             + "   all(func: uid(%s)) {"
             + System.lineSeparator()
             + "      uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetExpandedInteractions::formattedInteractionAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxInteractionFields()
                         .stream()
                         .map(QueryGetExpandedInteractions::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + interactionDemographicFields
             + System.lineSeparator()
             + "      ~GoldenRecord.interactions @facets(score) {"
             + System.lineSeparator()
             + "         uid"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(QueryGetExpandedInteractions::formattedGoldenRecordAdditionalNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxGoldenRecordFields()
                         .stream()
                         .map(QueryGetExpandedInteractions::formattedUniqueGoldenRecordField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + goldenRecordDemographicFields
             + System.lineSeparator()
             + "      }"
             + System.lineSeparator()
             + "   }"
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
