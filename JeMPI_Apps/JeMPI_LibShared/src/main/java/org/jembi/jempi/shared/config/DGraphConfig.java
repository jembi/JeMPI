package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.dgraph.DemographicDataFields;
import org.jembi.jempi.shared.config.dgraph.MutationCreateInteractionFields;
import org.jembi.jempi.shared.config.dgraph.MutationCreateInteractionType;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;

public class DGraphConfig {

   public final List<Pair<String, Integer>> demographicDataFields;
   public final String mutationCreateInteractionFields;
   public final String mutationCreateInteractionType;

   DGraphConfig(final JsonConfig jsonConfig) {
      demographicDataFields = DemographicDataFields.create(jsonConfig);
      mutationCreateInteractionFields = MutationCreateInteractionFields.create(jsonConfig);
      mutationCreateInteractionType = MutationCreateInteractionType.create(jsonConfig);
   }

}
