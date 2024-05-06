package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.dgraph.*;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;

public class DGraphConfig {

   public static final String PREDICATE_GOLDEN_RECORD_AUX_DATE_CREATED = "GoldenRecord.aux_date_created";
   public static final String PREDICATE_GOLDEN_RECORD_AUX_AUTO_UPDATE_ENABLED = "GoldenRecord.aux_auto_update_enabled";
   public static final String PREDICATE_GOLDEN_RECORD_AUX_ID = "GoldenRecord.aux_id";
   public static final String PREDICATE_GOLDEN_RECORD_INTERACTIONS = "GoldenRecord.interactions";

   public static final String PREDICATE_INTERACTION_AUX_DATE_CREATED = "Interaction.aux_date_created";
   public static final String PREDICATE_INTERACTION_AUX_ID = "Interaction.aux_id";
   public static final String PREDICATE_INTERACTION_AUX_CLINICAL_DATA = "Interaction.aux_clinical_data";

   public final List<Pair<String, Integer>> demographicDataFields;
   public final String mutationCreateInteractionFields;
   public final String mutationCreateInteractionType;
   public final String mutationCreateGoldenRecordFields;
   public final String mutationCreateGoldenRecordType;
   public final String mutationCreateAdditionalNodeFields;
   public final String mutationCreateAdditionalNodeType;
   public final String mutationCreateInteractionTriple;
   public final String mutationCreateLinkedGoldenRecordTriple;
   public final String goldenRecordFieldNames;
   public final String expandedGoldenRecordFieldNames;
   public final String interactionFieldNames;
   public final String expandedInteractionFieldNames;
   public final String queryGetInteractionByUid;
   public final String queryGetGoldenRecordByUid;
   public final String queryGetExpandedInteractions;
   public final String queryGetGoldenRecords;
   public final String queryGetExpandedGoldenRecords;

   public DGraphConfig(final JsonConfig jsonConfig) {
      demographicDataFields = DemographicDataFields.create(jsonConfig);
      mutationCreateInteractionFields = MutationCreateInteractionFields.create(jsonConfig);
      mutationCreateInteractionType = MutationCreateInteractionType.create(jsonConfig);
      mutationCreateGoldenRecordFields = MutationCreateGoldenRecordFields.create(jsonConfig);
      mutationCreateGoldenRecordType = MutationCreateGoldenRecordType.create(jsonConfig);
      mutationCreateAdditionalNodeFields = MutationCreateAdditionalNodeFields.create(jsonConfig);
      mutationCreateAdditionalNodeType = MutationCreateAdditionalNodeType.create(jsonConfig);
      mutationCreateInteractionTriple = MutationCreateInteractionTriple.create(jsonConfig);
      mutationCreateLinkedGoldenRecordTriple = MutationCreateLinkedGoldenRecordTriple.create(jsonConfig);
      goldenRecordFieldNames = GoldenRecordFieldNames.create(jsonConfig);
      expandedGoldenRecordFieldNames = ExpandedGoldenRecordFieldNames.create(jsonConfig);
      interactionFieldNames = InteractionFieldNames.create(jsonConfig);
      expandedInteractionFieldNames = ExpandedInteractionFieldNames.create(jsonConfig);
      queryGetInteractionByUid = QueryGetInteractionByUid.create(jsonConfig);
      queryGetGoldenRecordByUid = QueryGetGoldenRecordByUid.create(jsonConfig);
      queryGetExpandedInteractions = QueryGetExpandedInteractions.create(jsonConfig);
      queryGetGoldenRecords = QueryGetGoldenRecords.create(jsonConfig);
      queryGetExpandedGoldenRecords = QueryGetExpandedGoldenRecords.create(jsonConfig);
   }

}
