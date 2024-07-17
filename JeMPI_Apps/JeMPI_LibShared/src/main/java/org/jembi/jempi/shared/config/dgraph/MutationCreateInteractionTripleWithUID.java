package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateInteractionTripleWithUID {

   private MutationCreateInteractionTripleWithUID() {
   }

   public static String create(final JsonConfig jsonConfig) {
      return """
             <%s>  <Interaction.source_id>                     <%s> .
             <%s>  <Interaction.aux_date_created>              %s^^<xs:dateTime> .
             """
             + IntStream.range(0, jsonConfig.auxInteractionFields().size())
                        .filter(i -> !(jsonConfig.auxInteractionFields().get(i).scFieldName().equals("aux_date_created")))
                        .mapToObj(i -> "<%%s>  <Interaction.%-30s %%s .".formatted(
                              jsonConfig.auxInteractionFields().get(i).scFieldName() + ">")).collect(
                  Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(demographicField -> "<%%s>  <Interaction.%-30s %%s .".formatted(demographicField.scFieldName() + ">"))
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + """              
               <%s>  <dgraph.type>                               "Interaction" .
               """;
   }

}
