package org.jembi.jempi.shared.config;

import org.jembi.jempi.shared.config.input.JsonConfig;
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
         new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                           new DemographicData.Field("familyName", FAMILY_NAME),
                                           new DemographicData.Field("gender", GENDER),
                                           new DemographicData.Field("dob", DOB),
                                           new DemographicData.Field("city", CITY),
                                           new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                           new DemographicData.Field("nationalId", NATIONAL_ID)));
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
      return LinkerConfig.runDeterministicPrograms(LINKER_CONFIG.deterministicValidatePrograms, interaction, goldenRecord);
   }

   static boolean matchNotificationDeterministicMatch(
         final DemographicData goldenRecord,
         final DemographicData interaction) {
      return LinkerConfig.runDeterministicPrograms(LINKER_CONFIG.deterministicMatchPrograms, interaction, goldenRecord);
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
   void testDeterministicLinkPrograms() {
      Assertions.assertTrue(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertTrue(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                              new DemographicData.Field("familyName", FAMILY_NAME),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.Field("nationalId", null))),
            GOLDEN_RECORD));
      Assertions.assertTrue(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertFalse(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                              new DemographicData.Field("familyName", FAMILY_NAME),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.Field("nationalId", null))),
            GOLDEN_RECORD));
   }

   @Test
   void testDeterministicValidatePrograms() {
      Assertions.assertFalse(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertFalse(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_1.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                              new DemographicData.Field("familyName", FAMILY_NAME),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.Field("nationalId", null))),
            GOLDEN_RECORD));
      Assertions.assertFalse(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", NATIONAL_ID))),
            GOLDEN_RECORD));
      Assertions.assertTrue(LinkerConfig.runDeterministicPrograms(
            LINKER_CONFIG_2.deterministicValidatePrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                              new DemographicData.Field("familyName", FAMILY_NAME),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.Field("nationalId", null))),
            GOLDEN_RECORD));
   }

   @Test
   void testDeterministicCanApplyLinkingPrograms() {
      Assertions.assertTrue(LINKER_CONFIG_1.canApplyLinking(
            LINKER_CONFIG_1.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", null)))));
      Assertions.assertFalse(LINKER_CONFIG_2.canApplyLinking(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", null)))));
      Assertions.assertFalse(LINKER_CONFIG_2.canApplyLinking(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", GIVEN_NAME),
                                              new DemographicData.Field("familyName", FAMILY_NAME),
                                              new DemographicData.Field("gender", GENDER),
                                              new DemographicData.Field("dob", DOB),
                                              new DemographicData.Field("city", CITY),
                                              new DemographicData.Field("phoneNumber", PHONE_NUMBER),
                                              new DemographicData.Field("nationalId", null)))));
      Assertions.assertTrue(LINKER_CONFIG_2.canApplyLinking(
            LINKER_CONFIG_2.deterministicLinkPrograms,
            new DemographicData(Arrays.asList(new DemographicData.Field("givenName", null),
                                              new DemographicData.Field("familyName", null),
                                              new DemographicData.Field("gender", null),
                                              new DemographicData.Field("dob", null),
                                              new DemographicData.Field("city", null),
                                              new DemographicData.Field("phoneNumber", null),
                                              new DemographicData.Field("nationalId", NATIONAL_ID)))));
   }

}