package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.dgraph.*;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;

public class DGraphConfig {

   public final List<Pair<String, Integer>> demographicDataFields;
   public final String mutationCreateInteractionFields;
   public final String mutationCreateInteractionType;
   public final String mutationCreateGoldenRecordFields;
   public final String mutationCreateGoldenRecordType;
   public final String mutationCreateAdditionalNodeFields;
   public final String mutationCreateAdditionalNodeType;
   public final String goldenRecordFieldNames;
   public final String expandedGoldenRecordFieldNames;
   public final String interactionFieldNames;
   public final String expandedInteractionFieldNames;
   public final String queryGetInteractionByUid;

   DGraphConfig(final JsonConfig jsonConfig) {
      demographicDataFields = DemographicDataFields.create(jsonConfig);
      mutationCreateInteractionFields = MutationCreateInteractionFields.create(jsonConfig);
      mutationCreateInteractionType = MutationCreateInteractionType.create(jsonConfig);
      mutationCreateGoldenRecordFields = MutationCreateGoldenRecordFields.create(jsonConfig);
      mutationCreateGoldenRecordType = MutationCreateGoldenRecordType.create(jsonConfig);
      mutationCreateAdditionalNodeFields = MutationCreateAdditionalNodeFields.create(jsonConfig);
      mutationCreateAdditionalNodeType = MutationCreateAdditionalNodeType.create(jsonConfig);
      goldenRecordFieldNames = GoldenRecordFieldNames.create(jsonConfig);
      expandedGoldenRecordFieldNames = ExpandedGoldenRecordFieldNames.create(jsonConfig);
      interactionFieldNames = InteractionFieldNames.create(jsonConfig);
      expandedInteractionFieldNames = ExpandedInteractionFieldNames.create(jsonConfig);
      queryGetInteractionByUid = QueryGetInteractionByUid.create(jsonConfig);
   }

}
