package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueInteractionField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateInteractionFields {

   private MutationCreateInteractionFields() {
   }

   private static String formattedInteractionField(final int idx, final DemographicField field) {
//      final var name = field.fieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = field.indexInteraction() == null
            ? ""
            : field.indexInteraction();
      return String.format(Locale.ROOT, "Interaction.demographic_field_%02d:         %-10s%-35s.", idx, type, index);
   }

   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      final var name = field.fieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = "";
      return String.format(Locale.ROOT, "Interaction.%-30s%-10s%-35s.", name, type, index);
   }

   private static String formattedAdditionalInteractionNodes(final AdditionalNode additionalNode) {
      final var name = AppUtils.camelToSnake(additionalNode.nodeName()) + ":";
      return String.format(Locale.ROOT,
                           "Interaction.%-30s%-10s%-35s.",
                           name.startsWith("_")
                                 ? name.substring(1)
                                 : name,
                           "uid", "");
   }

   public static String create(final JsonConfig jsonConfig) {
      final var demographicFields = IntStream.range(0, jsonConfig.demographicFields().size())
                                             .mapToObj(i -> formattedInteractionField(i, jsonConfig.demographicFields().get(i)))
                                             .collect(Collectors.joining(System.lineSeparator()));
      return jsonConfig.additionalNodes()
                       .stream()
                       .map(MutationCreateInteractionFields::formattedAdditionalInteractionNodes)
                       .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(MutationCreateInteractionFields::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
//             + jsonConfig.demographicFields()
//                         .stream()
//                         .map(MutationCreateInteractionFields::formattedInteractionField)
//                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator();
   }

}
