package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.linker.Programs;
import org.jembi.jempi.shared.models.DemographicData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.jembi.jempi.shared.config.Config.LINKER_CONFIG;

class TestLinkerConfig {

   private static final String GIVEN_NAME = "givenName1";
   private static final String FAMILY_NAME = "familyName1";
   private static final String GENDER = "gender1";
   private static final String DOB = "dob1";
   private static final String CITY = "city1";
   private static final String PHONE_NUMBER = "phoneNumber1";
   private static final String NATIONAL_ID = "nationalId1";

   private static final DemographicData GOLDEN_RECORD =
         new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                           new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                           new DemographicData.DemographicField("gender", GENDER),
                                           new DemographicData.DemographicField("dob", DOB),
                                           new DemographicData.DemographicField("city", CITY),
                                           new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                           new DemographicData.DemographicField("nationalId", NATIONAL_ID)));
   private static JsonConfig JSON_CONFIG_1;
   private static JsonConfig JSON_CONFIG_2;
   private static LinkerConfig LINKER_CONFIG_1;
   private static LinkerConfig LINKER_CONFIG_2;

   @BeforeAll
   static void parseJsonConfig1() {
      JSON_CONFIG_1 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_11);
      LINKER_CONFIG_1 = new LinkerConfig(JSON_CONFIG_1);

      JSON_CONFIG_2 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_12);
      LINKER_CONFIG_2 = new LinkerConfig(JSON_CONFIG_2);
   }

   static boolean validateDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return Programs.runDeterministicPrograms(LINKER_CONFIG.deterministicValidatePrograms, interaction, goldenRecord);
   }

   static boolean matchNotificationDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return Programs.runDeterministicPrograms(LINKER_CONFIG.deterministicMatchPrograms, interaction, goldenRecord);
   }

   @Test
   void testSize() {
      Assertions.assertEquals(7, JSON_CONFIG_1.demographicFields().size());
      Assertions.assertEquals(7, JSON_CONFIG_2.demographicFields().size());
   }

   @Test
   void testCountPrograms() {
      Assertions.assertEquals(2, LINKER_CONFIG_1.deterministicLinkPrograms.size());
      Assertions.assertEquals(1, LINKER_CONFIG_2.deterministicLinkPrograms.size());
   }

   @Test
   void testSelectQueryLinkDeterministic() {
      Assertions.assertEquals(TestConstants.SELECT_QUERY_LINK_DETERMINISTIC_A_1, LINKER_CONFIG_1.deterministicLinkPrograms.getFirst().selectQuery());
      Assertions.assertEquals(TestConstants.SELECT_QUERY_LINK_DETERMINISTIC_B_1, LINKER_CONFIG_1.deterministicLinkPrograms.get(1).selectQuery());
   }

   @Test
   void testDeterministicLinkPrograms() {
      Assertions.assertTrue(Programs.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertTrue(Programs.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                              new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.DemographicField("nationalId", null))),
            GOLDEN_RECORD));
      Assertions.assertTrue(Programs.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertFalse(Programs.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                              new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.DemographicField("nationalId", null))),
            GOLDEN_RECORD));
   }

   @Test
   void testDeterministicValidatePrograms() {
      Assertions.assertFalse(Programs.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertFalse(Programs.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                              new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.DemographicField("nationalId", null))),
            GOLDEN_RECORD));
      Assertions.assertFalse(Programs.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertTrue(Programs.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                              new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.DemographicField("nationalId", null))),
            GOLDEN_RECORD));
   }

   @Test
   void testDeterministicCanApplyLinkingPrograms() {
      Assertions.assertTrue(Programs.canApplyLinking(
            LINKER_CONFIG_1.probabilisticLinkFields,
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", null)))));
      Assertions.assertFalse(Programs.canApplyLinking(
            LINKER_CONFIG_2.probabilisticLinkFields,
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", null)))));
      Assertions.assertFalse(Programs.canApplyLinking(
            LINKER_CONFIG_2.probabilisticLinkFields,
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", GIVEN_NAME),
                                              new DemographicData.DemographicField("familyName", FAMILY_NAME),
                                              new DemographicData.DemographicField("gender", GENDER),
                                              new DemographicData.DemographicField("dob", DOB),
                                              new DemographicData.DemographicField("city", CITY),
                                              new DemographicData.DemographicField("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.DemographicField("nationalId", null)))));
      Assertions.assertTrue(Programs.canApplyLinking(
            LINKER_CONFIG_2.probabilisticLinkFields,
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.DemographicField("givenName", null),
                                              new DemographicData.DemographicField("familyName", null),
                                              new DemographicData.DemographicField("gender", null),
                                              new DemographicData.DemographicField("dob", null),
                                              new DemographicData.DemographicField("city", null),
                                              new DemographicData.DemographicField("phoneNumber", null),
                                              new DemographicData.DemographicField("nationalId", NATIONAL_ID)))));
   }

}