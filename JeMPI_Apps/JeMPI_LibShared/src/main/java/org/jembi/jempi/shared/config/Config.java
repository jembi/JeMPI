package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;

public final class Config {

   private static final JsonConfig JSON_CONFIGURATION = JsonConfig.fromJson("config-reference.json");
//   private static final JsonConfig JSON_CONFIGURATION = JsonConfig.fromJson("config-reference-link-d-validate-dp-match-dp.json");
   public static final InputInterface INPUT_INTERFACE = new InputInterface(JSON_CONFIGURATION);

   private Config() {
   }

   public static final class InputInterface {
      public final List<Pair<String, Integer>> demographicDataCsvCols;

      private InputInterface(final JsonConfig jsonConfig) {
         demographicDataCsvCols =
               jsonConfig.demographicFields().stream().map(f -> Pair.of(f.fieldName(), f.source().csvCol())).toList();
      }
   }

}
