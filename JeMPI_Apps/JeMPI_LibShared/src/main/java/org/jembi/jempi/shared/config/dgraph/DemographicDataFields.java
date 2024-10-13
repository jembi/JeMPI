package org.jembi.jempi.shared.config.dgraph;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public final class DemographicDataFields {

   private DemographicDataFields() {
   }

   public static List<Pair<String, Integer>> create(final JsonConfig jsonConfig) {
      final var configVar = new ArrayList<Pair<String, Integer>>();
      for (int i = 0; i < jsonConfig.demographicFields().size(); i++) {
         configVar.add(Pair.of(AppUtils.camelToSnake(jsonConfig.demographicFields().get(i).scFieldName()), i));
      }
      return configVar;
   }

}
