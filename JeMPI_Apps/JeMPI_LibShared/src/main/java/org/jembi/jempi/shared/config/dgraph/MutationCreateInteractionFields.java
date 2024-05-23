package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AuxInteractionField;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateInteractionFields {

   private MutationCreateInteractionFields() {
   }

   private static String formattedInteractionField(final DemographicField field) {
      final var type = field.fieldType().toLowerCase(Locale.ROOT);
      final var index = field.indexInteraction() == null
            ? ""
            : field.indexInteraction();
      return String.format(Locale.ROOT, "Interaction.%-29s %-10s%-35s.", field.scFieldName() + ":", type, index);
   }

   private static String formattedUniqueInteractionField(final AuxInteractionField field) {
      final var name = field.scFieldName() + ":";
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
      final var demographicFields =
            jsonConfig.demographicFields().stream().map(MutationCreateInteractionFields::formattedInteractionField)
                      .collect(Collectors.joining(System.lineSeparator()));
      return jsonConfig.additionalNodes()
                       .stream()
                       .map(MutationCreateInteractionFields::formattedAdditionalInteractionNodes)
                       .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxInteractionFields()
                         .stream()
                         .map(MutationCreateInteractionFields::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
             + System.lineSeparator();
   }

}
