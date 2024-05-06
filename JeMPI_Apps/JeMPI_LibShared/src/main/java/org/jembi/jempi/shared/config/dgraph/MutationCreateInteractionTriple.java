package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateInteractionTriple {

   private MutationCreateInteractionTriple() {
   }

   public static String create(final JsonConfig jsonConfig) {
      return """
             _:%s  <Interaction.source_id>                     <%s>                  .
             _:%s  <Interaction.aux_date_created>              %s^^<xs:dateTime>     .
             _:%s  <Interaction.aux_id>                        %s                    .
             _:%s  <Interaction.aux_clinical_data>             %s                    ."""
             + System.lineSeparator()
             + IntStream.range(0, jsonConfig.demographicFields().size())
                        .mapToObj("_:%%s  <Interaction.demographic_field_%02d>          %%s                    ."::formatted)
                        .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + """              
               _:%s  <dgraph.type>                               "Interaction"         .
               """;
   }

}
