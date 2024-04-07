package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DGraphConfigTests {

   private static final JsonConfig JSON_CONFIG_1 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_11);
   private static final DGraphConfig DGRAPH_CONFIG_1 = new DGraphConfig(JSON_CONFIG_1);

   private static final JsonConfig JSON_CONFIG_2 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_12);
   private static final DGraphConfig DGRAPH_CONFIG_2 = new DGraphConfig(JSON_CONFIG_2);

   @Test
   void testDGraphConfig() {
      Assertions.assertEquals(7, JSON_CONFIG_1.demographicFields().size());
      Assertions.assertEquals(7, DGRAPH_CONFIG_1.demographicDataFields.size());
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_FIELDS_1,
                              DGRAPH_CONFIG_1.mutationCreateInteractionFields);
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_TYPE_1,
                              DGRAPH_CONFIG_1.mutationCreateInteractionType);

      Assertions.assertEquals(7, JSON_CONFIG_2.demographicFields().size());
      Assertions.assertEquals(7, DGRAPH_CONFIG_2.demographicDataFields.size());
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_FIELDS_2,
                              DGRAPH_CONFIG_2.mutationCreateInteractionFields);
      Assertions.assertEquals(TestConstants.MUTATION_CREATE_INTERACTION_TYPE_2,
                              DGRAPH_CONFIG_2.mutationCreateInteractionType);
   }

}
