package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DGraphConfigTests {

   private static JsonConfig JSON_CONFIG_1;
   private static DGraphConfig DGRAPH_CONFIG_1;

   private static JsonConfig JSON_CONFIG_2;
   private static DGraphConfig DGRAPH_CONFIG_2;

   @BeforeAll
   static void parseJsonConfig1() {
      JSON_CONFIG_1 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_11);
      DGRAPH_CONFIG_1 = new DGraphConfig(JSON_CONFIG_1);

      JSON_CONFIG_2 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_12);
      DGRAPH_CONFIG_2 = new DGraphConfig(JSON_CONFIG_2);
   }

   @Test
   void testSize() {
      Assertions.assertEquals(7, JSON_CONFIG_1.demographicFields().size());
      Assertions.assertEquals(7, DGRAPH_CONFIG_1.demographicDataFields.size());
      Assertions.assertEquals(7, JSON_CONFIG_2.demographicFields().size());
      Assertions.assertEquals(7, DGRAPH_CONFIG_2.demographicDataFields.size());
   }

   @Test
   void testMutationCreateInteractionFields() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_FIELDS_1,
                              DGRAPH_CONFIG_1.mutationCreateInteractionFields,
                              "MUTATION_CREATE_INTERACTION_FIELDS_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_FIELDS_2,
                              DGRAPH_CONFIG_2.mutationCreateInteractionFields,
                              "MUTATION_CREATE_INTERACTION_FIELDS_2");
   }

   @Test
   void testMutationCreateInteractionType() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_TYPE_1,
                              DGRAPH_CONFIG_1.mutationCreateInteractionType,
                              "MUTATION_CREATE_INTERACTION_TYPE_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_TYPE_2,
                              DGRAPH_CONFIG_2.mutationCreateInteractionType,
                              "MUTATION_CREATE_INTERACTION_TYPE_2");
   }

   @Test
   void testMutationCreateGoldenRecordFields() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_GOLDEN_RECORD_FIELDS_1,
                              DGRAPH_CONFIG_1.mutationCreateGoldenRecordFields,
                              "MUTATION_CREATE_GOLDEN_RECORD_FIELDS_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_GOLDEN_RECORD_FIELDS_2,
                              DGRAPH_CONFIG_2.mutationCreateGoldenRecordFields,
                              "MUTATION_CREATE_GOLDEN_RECORD_FIELDS_2");
   }

   @Test
   void testMutationCreateGoldenRecordType() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_GOLDEN_RECORD_TYPE_1,
                              DGRAPH_CONFIG_1.mutationCreateGoldenRecordType,
                              "MUTATION_CREATE_GOLDEN_RECORD_TYPE_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_GOLDEN_RECORD_TYPE_2,
                              DGRAPH_CONFIG_2.mutationCreateGoldenRecordType,
                              "MUTATION_CREATE_GOLDEN_RECORD_TYPE_2");
   }

   @Test
   void testMutationCreateAdditionalNodeFields() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_SOURCE_ID_FIELDS_1,
                              DGRAPH_CONFIG_1.mutationCreateAdditionalNodeFields,
                              "MUTATION_CREATE_SOURCE_ID_FIELDS_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_SOURCE_ID_FIELDS_2,
                              DGRAPH_CONFIG_2.mutationCreateAdditionalNodeFields,
                              "MUTATION_CREATE_SOURECE_ID_TYPE_2");
   }

   @Test
   void testMutationCreateAdditionalNodeType() {
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_SOURCE_ID_TYPE_1,
                              DGRAPH_CONFIG_1.mutationCreateAdditionalNodeType,
                              "MUTATION_CREATE_SOURCE_ID_TYPE_1");
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_SOURCE_ID_TYPE_2,
                              DGRAPH_CONFIG_2.mutationCreateAdditionalNodeType,
                              "MUTATION_CREATE_SOURCE_ID_TYPE_2");
   }

   @Test
   void testGoldenRecordFieldNames() {
      Assertions.assertEquals(TestConstants.GOLDEN_RECORD_FIELD_NAMES_1,
                              DGRAPH_CONFIG_1.goldenRecordFieldNames,
                              "GOLDEN_RECORD_FIELD_NAMES_1");
      Assertions.assertEquals(TestConstants.GOLDEN_RECORD_FIELD_NAMES_2,
                              DGRAPH_CONFIG_2.goldenRecordFieldNames,
                              "GOLDEN_RECORD_FIELD_NAMES_2");
   }

   @Test
   void testExpandedGoldenRecordFieldNames() {
      Assertions.assertEquals(TestConstants.EXPANDED_GOLDEN_RECORD_FIELD_NAMES_1,
                              DGRAPH_CONFIG_1.expandedGoldenRecordFieldNames,
                              "EXPANDED_GOLDEN_RECORD_FIELD_NAMES_1");
      Assertions.assertEquals(TestConstants.EXPANDED_GOLDEN_RECORD_FIELD_NAMES_2,
                              DGRAPH_CONFIG_2.expandedGoldenRecordFieldNames,
                              "EXPANDED_GOLDEN_RECORD_FIELD_NAMES_2");
   }

   @Test
   void testInteractionFieldNames() {
      Assertions.assertEquals(TestConstants.INTERACTION_FIELD_NAMES_1,
                              DGRAPH_CONFIG_1.interactionFieldNames,
                              "INTERACTION_FIELD_NAMES_1");
      Assertions.assertEquals(TestConstants.INTERACTION_FIELD_NAMES_2,
                              DGRAPH_CONFIG_2.interactionFieldNames,
                              "INTERACTION_FIELD_NAMES_2");
   }

   @Test
   void testExpandedInteractionFieldNames() {
      Assertions.assertEquals(TestConstants.EXPANDED_INTERACTION_FIELD_NAMES_1,
                              DGRAPH_CONFIG_1.expandedInteractionFieldNames,
                              "EXPANDED_INTERACTION_FIELD_NAMES_1");
      Assertions.assertEquals(TestConstants.EXPANDED_INTERACTION_FIELD_NAMES_2,
                              DGRAPH_CONFIG_2.expandedInteractionFieldNames,
                              "EXPANDED_INTERACTION_FIELD_NAMES_2");
   }

   @Test
   void testQueryGetInteractionByUid() {
      Assertions.assertEquals(TestConstants.QUERY_GET_INTERACTION_BY_UID_1,
                              DGRAPH_CONFIG_1.queryGetInteractionByUid,
                              "QUERY_GET_INTERACTION_BY_UID_1");
      Assertions.assertEquals(TestConstants.QUERY_GET_INTERACTION_BY_UID_2,
                              DGRAPH_CONFIG_2.queryGetInteractionByUid,
                              "QUERY_GET_INTERACTION_BY_UID_2");
   }

   @Test
   void testQueryGetGoldenRecordByUid() {
      Assertions.assertEquals(TestConstants.QUERY_GET_GOLDEN_RECORD_BY_UID_1,
                              DGRAPH_CONFIG_1.queryGetGoldenRecordByUid,
                              "QUERY_GET_GOLDEN_RECORD_BY_UID_1");
      Assertions.assertEquals(TestConstants.QUERY_GET_GOLDEN_RECORD_BY_UID_2,
                              DGRAPH_CONFIG_2.queryGetGoldenRecordByUid,
                              "QUERY_GET_GOLDEN_RECORD_BY_UID_2");
   }

   @Test
   void testQueryGetExpandedInteractions() {
      Assertions.assertEquals(TestConstants.QUERY_GET_EXPANDED_INTERACTIONS_1,
                              DGRAPH_CONFIG_1.queryGetExpandedInteractions,
                              "QUERY_GET_EXPANDED_INTERACTIONS_1");
      Assertions.assertEquals(TestConstants.QUERY_GET_EXPANDED_INTERACTIONS_2,
                              DGRAPH_CONFIG_2.queryGetExpandedInteractions,
                              "QUERY_GET_EXPANDED_INTERACTIONS_2");
   }

   @Test
   void testQueryGetGoldenRecords() {
      Assertions.assertEquals(TestConstants.QUERY_GET_GOLDEN_RECORDS_1,
                              DGRAPH_CONFIG_1.queryGetGoldenRecords,
                              "QUERY_GET_GOLDEN_RECORDS_1");
      Assertions.assertEquals(TestConstants.QUERY_GET_GOLDEN_RECORDS_2,
                              DGRAPH_CONFIG_2.queryGetGoldenRecords,
                              "QUERY_GET_GOLDEN_RECORDS_2");
   }

   @Test
   void testQueryGetExpandedGoldenRecords() {
      Assertions.assertEquals(TestConstants.QUERY_GET_EXPANDED_GOLDEN_RECORDS_1,
                              DGRAPH_CONFIG_1.queryGetExpandedGoldenRecords,
                              "QUERY_GET_EXPANDED_GOLDEN_RECORDS_1");
      Assertions.assertEquals(TestConstants.QUERY_GET_EXPANDED_GOLDEN_RECORDS_2,
                              DGRAPH_CONFIG_2.queryGetExpandedGoldenRecords,
                              "QUERY_GET_EXPANDED_GOLDEN_RECORDS_2");
   }

}
