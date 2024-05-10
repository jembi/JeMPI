package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

class LibMpiDGraphTests {

   private static final LocalDateTime NOW;
   private static final String GID_1;
   private static final String SID_1;
   private static final String FID_1;
   private static final String PID_1;
   private static final String SID_2;
   private static final String FID_2;
   private static final String PID_2;
   private static final String AUX_ID_1;
   private static final ArrayList<String> AUX_FIELDS_1;
   private static final ArrayList<String> DEMOGRAPHIC_FIELDS_1;

   private static final String GIVEN_NAME_1;
   private static final String FAMILY_NAME_1;
   private static final String GENDER_1;
   private static final String DOB_1;
   private static final String CITY_1;
   private static final String PHONE_NUMBER_1;
   private static final String NATIONAL_ID_1;
   private static final DateTimeFormatter DTF;
   private static JsonConfig JSON_CONFIG_1;
   private static JsonConfig JSON_CONFIG_2;

   static {
      DTF = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
      NOW = LocalDateTime.now();
      GID_1 = "GID_1";
      SID_1 = "SID_1";
      FID_1 = "FID_1";
      PID_1 = "PID_1";
      SID_2 = "SID_2";
      FID_2 = "FID_2";
      PID_2 = "PID_2";
      AUX_ID_1 = "AUX_ID_1";
      GIVEN_NAME_1 = "GIVEN_NAME_1";
      FAMILY_NAME_1 = "FAMILY_NAME_1";
      GENDER_1 = "GENDER_1";
      DOB_1 = "DOB_1";
      CITY_1 = "CITY_1";
      PHONE_NUMBER_1 = "PHONE_NUMBER_1";
      NATIONAL_ID_1 = "NATIONAL_ID_1";
      AUX_FIELDS_1 =
            new ArrayList<>(List.of(NOW.toString(), "true", AUX_ID_1));
      DEMOGRAPHIC_FIELDS_1 =
            new ArrayList<>(List.of(GIVEN_NAME_1, FAMILY_NAME_1, GENDER_1, DOB_1, CITY_1, PHONE_NUMBER_1, NATIONAL_ID_1));
   }

   @BeforeAll
   static void parseJsonConfig1() {
      JSON_CONFIG_1 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_11);
      JSON_CONFIG_2 = JsonConfig.fromJson(TestConstants.CONFIG_FILE_12);
   }

   @Test
   void testSize() {
      Assertions.assertEquals(7, JSON_CONFIG_1.demographicFields().size());
      Assertions.assertEquals(7, JSON_CONFIG_2.demographicFields().size());
   }

   @Test
   void testGoldenRecord() {
      final var json =
            """
            {
            	"uid": "0x177b",
            	"GoldenRecord.source_id": [
            		{
            			"uid": "0x1779",
            			"SourceId.facility": "FA1",
            			"SourceId.patient": "198910235001088"
            		},
            		{
            			"uid": "0x1b7c",
            			"SourceId.facility": "FA2",
            			"SourceId.patient": "198910235001088"
            		},
            		{
            			"uid": "0x1f99",
            			"SourceId.facility": "FA5",
            			"SourceId.patient": "198910235001088"
            		},
            		{
            			"uid": "0x209e",
            			"SourceId.facility": "FA3",
            			"SourceId.patient": "198910235001088"
            		}
            	],
            	"GoldenRecord.aux_date_created": "2024-05-10T13:06:25.359198023",
            	"GoldenRecord.aux_auto_update_enabled": true,
            	"GoldenRecord.aux_id": "rec-0000000800--4",
            	"GoldenRecord.demographic_field_00": "GivenName1",
            	"GoldenRecord.demographic_field_01": "FamilyName1",
            	"GoldenRecord.demographic_field_02": "female",
            	"GoldenRecord.demographic_field_03": "19891023",
            	"GoldenRecord.demographic_field_04": "City1",
            	"GoldenRecord.demographic_field_05": "0105355775",
            	"GoldenRecord.demographic_field_06": "198910235001088",
            	"GoldenRecord.interactions": [
            		{
            			"uid": "0x177a",
            			"Interaction.source_id": {
            				"uid": "0x1779",
            				"SourceId.facility": "FA1",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:30.218003326",
            			"Interaction.aux_id": "rec-0000000800--4",
            			"Interaction.aux_clinical_data": "RANDOM DATA(73)",
            			"Interaction.demographic_field_00": "GivenName1",
            			"Interaction.demographic_field_01": "FamilyName1",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "City1",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001088",
            			"GoldenRecord.interactions|score": 1
            		},
            		{
            			"uid": "0x1b7d",
            			"Interaction.source_id": {
            				"uid": "0x1b7c",
            				"SourceId.facility": "FA2",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:30.610151212",
            			"Interaction.aux_id": "rec-0000000800--1",
            			"Interaction.aux_clinical_data": "RANDOM DATA(682)",
            			"Interaction.demographic_field_00": "oscar",
            			"Interaction.demographic_field_01": "habwiau",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "luswka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "1989q0235001088",
            			"GoldenRecord.interactions|score": 0.934154
            		},
            		{
            			"uid": "0x1f9a",
            			"Interaction.source_id": {
            				"uid": "0x1f99",
            				"SourceId.facility": "FA5",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.075409853",
            			"Interaction.aux_id": "rec-0000000800--0",
            			"Interaction.aux_clinical_data": "RANDOM DATA(243)",
            			"Interaction.demographic_field_00": "oscar",
            			"Interaction.demographic_field_01": "habwizu",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "lusaka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001088",
            			"GoldenRecord.interactions|score": 1
            		},
            		{
            			"uid": "0x1fe1",
            			"Interaction.source_id": {
            				"uid": "0x1b7c",
            				"SourceId.facility": "FA2",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.107057136",
            			"Interaction.aux_id": "rec-0000000800--3",
            			"Interaction.aux_clinical_data": "RANDOM DATA(544)",
            			"Interaction.demographic_field_00": "oscar",
            			"Interaction.demographic_field_01": "habwizu",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "",
            			"Interaction.demographic_field_04": "lusaka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001088",
            			"GoldenRecord.interactions|score": 1
            		},
            		{
            			"uid": "0x209f",
            			"Interaction.source_id": {
            				"uid": "0x209e",
            				"SourceId.facility": "FA3",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.159584693",
            			"Interaction.aux_id": "rec-0000000800--7",
            			"Interaction.aux_clinical_data": "RANDOM DATA(89)",
            			"Interaction.demographic_field_00": "oscar",
            			"Interaction.demographic_field_01": "habqizu",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "lusaka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "",
            			"GoldenRecord.interactions|score": 0.829876
            		},
            		{
            			"uid": "0x216f",
            			"Interaction.source_id": {
            				"uid": "0x209e",
            				"SourceId.facility": "FA3",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.22006385",
            			"Interaction.aux_id": "rec-0000000800--5",
            			"Interaction.aux_clinical_data": "RANDOM DATA(123)",
            			"Interaction.demographic_field_00": "",
            			"Interaction.demographic_field_01": "habizu",
            			"Interaction.demographic_field_02": "",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "lusaka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001088",
            			"GoldenRecord.interactions|score": 1
            		},
            		{
            			"uid": "0x21ff",
            			"Interaction.source_id": {
            				"uid": "0x1b7c",
            				"SourceId.facility": "FA2",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.263791218",
            			"Interaction.aux_id": "rec-0000000800--6",
            			"Interaction.aux_clinical_data": "RANDOM DATA(824)",
            			"Interaction.demographic_field_00": "oscamr",
            			"Interaction.demographic_field_01": "habwizu",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "",
            			"Interaction.demographic_field_04": "lusaka",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001288",
            			"GoldenRecord.interactions|score": 0.831115
            		},
            		{
            			"uid": "0x2353",
            			"Interaction.source_id": {
            				"uid": "0x209e",
            				"SourceId.facility": "FA3",
            				"SourceId.patient": "198910235001088"
            			},
            			"Interaction.aux_date_created": "2024-05-10T13:05:31.369597157",
            			"Interaction.aux_id": "rec-0000000800--2",
            			"Interaction.aux_clinical_data": "RANDOM DATA(527)",
            			"Interaction.demographic_field_00": "oscr",
            			"Interaction.demographic_field_01": "",
            			"Interaction.demographic_field_02": "female",
            			"Interaction.demographic_field_03": "19891023",
            			"Interaction.demographic_field_04": "lusaks",
            			"Interaction.demographic_field_05": "0105355775",
            			"Interaction.demographic_field_06": "198910235001088",
            			"GoldenRecord.interactions|score": 1
            		}
            	]
            }
            """;
      final CustomDgraphExpandedGoldenRecord o1;
      try {
         o1 = OBJECT_MAPPER.readValue(json, CustomDgraphExpandedGoldenRecord.class);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }
      final var actual = DeprecatedCustomFunctions.toExpandedGoldenRecord(o1);
      final var expected = new ExpandedGoldenRecord(new GoldenRecord("0x177b",
                                                                     List.of(
                                                                           new CustomSourceId("0x1779",
                                                                                              "FA1",
                                                                                              "198910235001088"),
                                                                           new CustomSourceId("0x1b7c",
                                                                                              "FA2",
                                                                                              "198910235001088"),
                                                                           new CustomSourceId("0x1f99",
                                                                                              "FA5",
                                                                                              "198910235001088"),
                                                                           new CustomSourceId("0x209e",
                                                                                              "FA3",
                                                                                              "198910235001088")),

                                                                     new AuxGoldenRecordData(LocalDateTime.parse(
                                                                           "2024-05-10T13:06:25.359198023"),
                                                                                             true,
                                                                                             "rec-0000000800--4"),
                                                                     new DemographicData(Stream.of(new DemographicData.DemographicField(
                                                                                                         "givenName",
                                                                                                         "GivenName1"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "familyName",
                                                                                                         "FamilyName1"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "gender",
                                                                                                         "female"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "dob",
                                                                                                         "19891023"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "city",
                                                                                                         "City1"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "phoneNumber",
                                                                                                         "0105355775"),
                                                                                                   new DemographicData.DemographicField(
                                                                                                         "nationalId",
                                                                                                         "198910235001088"))
                                                                                               .collect(Collectors.toList()))),
                                                    Stream.of(new InteractionWithScore(
                                                                new Interaction("0x177a",
                                                                                new CustomSourceId("0x1779",
                                                                                                   "FA1",
                                                                                                   "198910235001088"),
                                                                                new AuxInteractionData(LocalDateTime.parse(
                                                                                      "2024-05-10T13:05:30.218003326"),
                                                                                                       "rec-0000000800--4",
                                                                                                       "RANDOM DATA(73)"),
                                                                                new DemographicData(
                                                                                      Stream.of(new DemographicData.DemographicField(
                                                                                                      "givenName",
                                                                                                      "GivenName1"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "familyName",
                                                                                                      "FamilyName1"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "gender",
                                                                                                      "female"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "dob",
                                                                                                      "19891023"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "city",
                                                                                                      "City1"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "phoneNumber",
                                                                                                      "0105355775"),
                                                                                                new DemographicData.DemographicField(
                                                                                                      "nationalId",
                                                                                                      "198910235001088")
                                                                                               )
                                                                                            .collect(Collectors.toList()))),
                                                                1.0F))
                                                          .collect(Collectors.toList()));
      Assertions.assertEquals(expected.goldenRecord().goldenId(), actual.goldenRecord().goldenId());
      Assertions.assertEquals(expected.goldenRecord().customUniqueGoldenRecordData(),
                              actual.goldenRecord().customUniqueGoldenRecordData());
      Assertions.assertEquals(expected.goldenRecord().demographicData().fields, actual.goldenRecord().demographicData().fields);
      final var e0 = expected.interactionsWithScore().getFirst();
      final var e0i = e0.interaction();
      final var a0 = actual.interactionsWithScore().getFirst();
      final var a0i = a0.interaction();
      Assertions.assertEquals(e0.score(), a0.score());
      Assertions.assertEquals(e0i.interactionId(), a0i.interactionId());
      Assertions.assertEquals(e0i.sourceId(), a0i.sourceId());
      Assertions.assertEquals(e0i.demographicData().fields, a0i.demographicData().fields);

   }

}
