package org.jembi.jempi.shared.config.dgraph;

import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class MutationCreateLinkedGoldenRecordTriple {

   private MutationCreateLinkedGoldenRecordTriple() {
   }

   public static String create(final JsonConfig jsonConfig) {
      return """
             _:%s  <GoldenRecord.source_id>                     <%s>                  .
             _:%s  <GoldenRecord.aux_date_created>              %s^^<xs:dateTime>     .
             _:%s  <GoldenRecord.aux_auto_update_enabled>       %s^^<xs:boolean>      .
             _:%s  <GoldenRecord.aux_id>                        %s                    ."""
             + System.lineSeparator()
             + IntStream.range(0, jsonConfig.demographicFields().size())
                        .mapToObj("_:%%s  <GoldenRecord.demographic_field_%02d>          %%s                    ."::formatted)
                        .collect(Collectors.joining(System.lineSeparator()))
             + System.lineSeparator()
             + """                              
               _:%s  <GoldenRecord.interactions>                  <%s> (score=%f)       .
               _:%s  <dgraph.type>                                "GoldenRecord"        .
               """;
   }

}
