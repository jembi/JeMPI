package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNodes;
import org.jembi.jempi.shared.config.input.DemographicField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.UniqueInteractionField;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateInteractionType {

   private MutationCreateInteractionType() {
   }

   private static String formattedInteractionField(final DemographicField field) {
      final var name = field.fieldName();
      return String.format(Locale.ROOT, "   Interaction.%s", name);
   }

   private static String formattedUniqueInteractionField(final UniqueInteractionField field) {
      final var name = field.fieldName();
      return String.format(Locale.ROOT, "   Interaction.%s", name);
   }

   private static String formattedAdditionalInteractionNodes(final AdditionalNodes additionalNodes) {
      final var snake = AppUtils.camelToSnake(additionalNodes.nodeName()) + ":";
      final var name = snake.startsWith("_")
            ? snake.substring(1)
            : snake;
      return String.format(Locale.ROOT, "   Interaction.%-31s%s", name, additionalNodes.nodeName());
   }

   public static String create(final JsonConfig jsonConfig) {
      return "type Interaction {"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(MutationCreateInteractionType::formattedAdditionalInteractionNodes)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.uniqueInteractionFields()
                         .stream()
                         .map(MutationCreateInteractionType::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(MutationCreateInteractionType::formattedInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
