package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateLinkedGoldenRecordTripleWithUID {

   private MutationCreateLinkedGoldenRecordTripleWithUID() {
   }

   public static String create(final JsonConfig jsonConfig) {
      return """
             <%s>  <GoldenRecord.source_id>                     <%s> .
             <%s>  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime> .
             <%s>  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean> ."""
             + System.lineSeparator()
             + IntStream.range(0, jsonConfig.auxGoldenRecordFields().size())
                        .filter(i -> !(jsonConfig.auxGoldenRecordFields().get(i).scFieldName().equals("aux_date_created")
                                       || jsonConfig.auxGoldenRecordFields()
                                                    .get(i)
                                                    .scFieldName()
                                                    .equals("aux_auto_update_enabled")))
                        .mapToObj(i -> "<%%s>  <GoldenRecord.%-30s %%s .".formatted(
                              jsonConfig.auxGoldenRecordFields().get(i).scFieldName() + ">")).collect(
                  Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + jsonConfig.demographicFields()
                         .stream()
                         .map(demographicField -> "<%%s>  <GoldenRecord.%-30s %%s .".formatted(demographicField.scFieldName() + ">"))
                         .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + """                              
               <%s>  <GoldenRecord.interactions>                  <%s> (score=%f) .
               <%s>  <dgraph.type>                                "GoldenRecord" .
               """;
   }

}
