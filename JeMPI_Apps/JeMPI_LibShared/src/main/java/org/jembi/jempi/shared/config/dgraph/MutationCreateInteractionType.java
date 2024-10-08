package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.AdditionalNode;
import org.jembi.jempi.shared.config.input.AuxInteractionField;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public final class MutationCreateInteractionType {

   private MutationCreateInteractionType() {
   }

   private static String formattedUniqueInteractionField(final AuxInteractionField field) {
      final var name = field.scFieldName();
      return String.format(Locale.ROOT, "   Interaction.%s", name);
   }

   private static String formattedAdditionalInteractionNode(final AdditionalNode additionalNode) {
      final var snake = AppUtils.camelToSnake(additionalNode.nodeName()) + ":";
      final var name = snake.startsWith("_")
            ? snake.substring(1)
            : snake;
      return String.format(Locale.ROOT, "   Interaction.%-31s%s", name, additionalNode.nodeName());
   }

   public static String create(final JsonConfig jsonConfig) {
      final var demographicFields = jsonConfig.demographicFields()
                                              .stream()
                                              .map(demographicField -> "   Interaction.%s".formatted(demographicField.scFieldName()))
                                              .collect(Collectors.joining(System.lineSeparator()));
      return "type Interaction {"
             + System.lineSeparator()
             + jsonConfig.additionalNodes()
                         .stream()
                         .map(MutationCreateInteractionType::formattedAdditionalInteractionNode)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.auxInteractionFields()
                         .stream()
                         .map(MutationCreateInteractionType::formattedUniqueInteractionField)
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + demographicFields
//             + jsonConfig.demographicFields()
//                         .stream()
//                         .map(MutationCreateInteractionType::formattedInteractionField)
//                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + "}"
             + System.lineSeparator();
   }

}
