package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

class TestInputInterfaceConfig {

   private static InputInterfaceConfig INPUT_INTERFACE_CONFIG_1;
   private static InputInterfaceConfig INPUT_INTERFACE_CONFIG_2;

   @BeforeAll
   static void parseJsonConfig1() {
      JsonConfig JSON_CONFIG_1 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_11);
      INPUT_INTERFACE_CONFIG_1 = new InputInterfaceConfig(JSON_CONFIG_1);

      JsonConfig JSON_CONFIG_2 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_12);
      INPUT_INTERFACE_CONFIG_2 = new InputInterfaceConfig(JSON_CONFIG_2);
   }

   @Test
   void testDemographicData() {
      Assertions.assertEquals(7, INPUT_INTERFACE_CONFIG_1.demographicDataSource.size());
      Assertions.assertEquals(7, INPUT_INTERFACE_CONFIG_2.demographicDataSource.size());
//      Assertions.assertEquals(List.of(Pair.of("given_name", new Source(1 null)),
//                                      Pair.of("family_name", 2),
//                                      Pair.of("gender", 3),
//                                      Pair.of("dob", 4),
//                                      Pair.of("city", 5),
//                                      Pair.of("phone_number", 6),
//                                      Pair.of("national_id", 7)),
//                              INPUT_INTERFACE_CONFIG_1.demographicDataCsvCols);
//      Assertions.assertEquals(List.of(Pair.of("given_name", 1),
//                                      Pair.of("family_name", 2),
//                                      Pair.of("gender", 3),
//                                      Pair.of("dob", 4),
//                                      Pair.of("city", 5),
//                                      Pair.of("phone_number", 6),
//                                      Pair.of("national_id", 7)),
//                              INPUT_INTERFACE_CONFIG_2.demographicDataCsvCols);
   }

   @Test
   void testAuxInteractionData() {
      Assertions.assertEquals(3, INPUT_INTERFACE_CONFIG_1.auxInteractionDataSource.size());
      Assertions.assertEquals(3, INPUT_INTERFACE_CONFIG_2.auxInteractionDataSource.size());
      Assertions.assertEquals(1, INPUT_INTERFACE_CONFIG_1.additionalNodesSource.size());
      Assertions.assertEquals(1, INPUT_INTERFACE_CONFIG_2.additionalNodesSource.size());
//      INPUT_INTERFACE_CONFIG_1.auxInteractionDataCsvCols.get("aux_id");
//      Assertions.assertNull(INPUT_INTERFACE_CONFIG_1.auxInteractionDataCsvCols.get("aux_date_created"));
//      Assertions.assertEquals(0, INPUT_INTERFACE_CONFIG_1.auxInteractionDataCsvCols.get("aux_id"));
//      Assertions.assertEquals(10, INPUT_INTERFACE_CONFIG_1.auxInteractionDataCsvCols.get("aux_clinical_data"));
//      Assertions.assertNull(INPUT_INTERFACE_CONFIG_2.auxInteractionDataCsvCols.get("aux_date_created"));
//      Assertions.assertEquals(0, INPUT_INTERFACE_CONFIG_2.auxInteractionDataCsvCols.get("aux_id"));
//      Assertions.assertEquals(10, INPUT_INTERFACE_CONFIG_2.auxInteractionDataCsvCols.get("aux_clinical_data"));
//      Assertions.assertSame(Map.ofEntries(
//                                    new AbstractMap.SimpleEntry<>("aux_date_created", null),
//                                    new AbstractMap.SimpleEntry<>("aux_id", 0),
//                                    new AbstractMap.SimpleEntry<>("aux_clinical_data", 11)),
//                              INPUT_INTERFACE_CONFIG_1.auxInteractionDataCsvCols);
//      Assertions.assertEquals(List.of(Pair.of("aux_date_created", null),
//                                      Pair.of("aux_id", 0),
//                                      Pair.of("aux_clinical_data", 10)),
//                              INPUT_INTERFACE_CONFIG_2.auxInteractionDataCsvCols);
//      Assertions.assertNull(INPUT_INTERFACE_CONFIG_1.auxDateCreatedCsvCol);
      Assertions.assertEquals(0, INPUT_INTERFACE_CONFIG_1.auxIdCsvCol);
      Assertions.assertEquals(10, INPUT_INTERFACE_CONFIG_1.auxClinicalDataCsvCol);
//      Assertions.assertNull(INPUT_INTERFACE_CONFIG_2.auxDateCreatedCsvCol);
      Assertions.assertEquals(0, INPUT_INTERFACE_CONFIG_2.auxIdCsvCol);
      Assertions.assertEquals(10, INPUT_INTERFACE_CONFIG_2.auxClinicalDataCsvCol);
   }

   @Test
   void testAdditionalNodes() {
      Assertions.assertEquals(1, INPUT_INTERFACE_CONFIG_1.additionalNodesSource.size());
      Assertions.assertEquals(1, INPUT_INTERFACE_CONFIG_2.additionalNodesSource.size());
      Assertions.assertEquals(
            new HashMap<>(
                  Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Map<String, Integer>>(
                              "SourceId", new HashMap<>(Map.ofEntries(
                              new AbstractMap.SimpleEntry<>("facility", 8),
                              new AbstractMap.SimpleEntry<>("patient", 9)))))),
            INPUT_INTERFACE_CONFIG_1.additionalNodesSource);
      Assertions.assertEquals(
            new HashMap<>(
                  Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Map<String, Integer>>(
                              "SourceId", new HashMap<>(Map.ofEntries(
                              new AbstractMap.SimpleEntry<>("facility", 8),
                              new AbstractMap.SimpleEntry<>("patient", 9)))))),
            INPUT_INTERFACE_CONFIG_2.additionalNodesSource);

      Assertions.assertEquals(8, INPUT_INTERFACE_CONFIG_1.sourceIdFacilityCsvCol);
      Assertions.assertEquals(9, INPUT_INTERFACE_CONFIG_1.sourceIdPatientCsvCol);
      Assertions.assertEquals(8, INPUT_INTERFACE_CONFIG_2.sourceIdFacilityCsvCol);
      Assertions.assertEquals(9, INPUT_INTERFACE_CONFIG_2.sourceIdPatientCsvCol);
   }


}
