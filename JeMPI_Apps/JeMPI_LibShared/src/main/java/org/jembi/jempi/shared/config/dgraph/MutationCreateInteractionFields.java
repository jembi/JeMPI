package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNodes;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueInteractionField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateInteractionFields {

   private MutationCreateInteractionFields() {
   }

   private static String formattedInteractionField(final DemographicField field) {
      final var name = field.fieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = field.indexInteraction() == null
            ? ""
            : field.indexInteraction();
      return String.format(Locale.ROOT, "Interaction.%-30s%-10s%-35s.", name, type, index);
   }

   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      final var name = field.fieldName() + ":";
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = "";
      return String.format(Locale.ROOT, "Interaction.%-30s%-10s%-35s.", name, type, index);
   }

   private static String formattedAdditionalInteractionNodes(final AdditionalNodes additionalNodes) {
      final var name = AppUtils.camelToSnake(additionalNodes.nodeName()) + ":";
      return String.format(Locale.ROOT,
                           "Interaction.%-30s%-10s%-35s.",
                           name.startsWith("_")
                                 ? name.substring(1)
                                 : name,
                           "uid", "");
   }

   public static String create(final JsonConfig jsonConfig) {
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
             + jsonConfig.demographicFields()
                         .stream()
                         .map(MutationCreateInteractionFields::formattedInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator();
   }

}
