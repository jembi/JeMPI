package org.jembi.jempi.libmpi.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.JSON_CONFIG;
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

/*
   @Test
   void testCustomDgraphGoldenRecord() throws JsonProcessingException {
      final var customDgraphGoldenRecord = new CustomDgraphGoldenRecord(GID_1,
                                                                        List.of(new DgraphSourceId(SID_1, FID_1, PID_1),
                                                                                new DgraphSourceId(SID_2, FID_2, PID_2)),
                                                                        NOW,
                                                                        true,
                                                                        AUX_ID_1,
                                                                        GIVEN_NAME_1,
                                                                        FAMILY_NAME_1,
                                                                        GENDER_1,
                                                                        DOB_1,
                                                                        CITY_1,
                                                                        PHONE_NUMBER_1,
                                                                        NATIONAL_ID_1);
      final var json = OBJECT_MAPPER.writeValueAsString(customDgraphGoldenRecord);
      final var dgraphGoldenRecordJsonNode = new JsonNodeGoldenRecord(OBJECT_MAPPER.readTree(json));
      final var t = dgraphGoldenRecordJsonNode.toGoldenRecord();

      Assertions.assertEquals(GID_1, dgraphGoldenRecordJsonNode.jsonNode().get("uid").textValue());

      for (int i = 0; i < JSON_CONFIG.additionalNodes().size(); i++) {
         final var elements = dgraphGoldenRecordJsonNode.jsonNode()
                                                        .get("GoldenRecord." + AppUtils.camelToSnake(JSON_CONFIG.additionalNodes()
                                                                                                                .get(i)
                                                                                                                .nodeName()))
                                                        .elements();
         while (elements.hasNext()) {
            final var element = elements.next();
            System.out.println(element);
            System.out.println(element.get("uid").textValue());
            System.out.println(element.get("SourceId.facility").textValue());
            System.out.println(element.get("SourceId.patient").textValue());
         }
      }

      JSON_CONFIG_1.additionalNodes().forEach(node -> {
         final var name = "GoldenRecord." + AppUtils.camelToSnake(node.nodeName());
         System.out.println(name);
         final var additionalNodes = dgraphGoldenRecordJsonNode.jsonNode().get(name).elements();

         int i = 0;
         while (additionalNodes.hasNext()) {
            final var next = additionalNodes.next();
            System.out.println(next);
            i += 1;
         }
         final var additionalMode1 = dgraphGoldenRecordJsonNode.jsonNode().get(name).get(0);
         Assertions.assertEquals(SID_1, additionalMode1.get("uid").textValue());
         Assertions.assertEquals(FID_1, additionalMode1.get("SourceId.facility").textValue());
         Assertions.assertEquals(PID_1, additionalMode1.get("SourceId.patient").textValue());
         final var additionalMode2 = dgraphGoldenRecordJsonNode.jsonNode().get(name).get(1);
         Assertions.assertEquals(SID_2, additionalMode2.get("uid").textValue());
         Assertions.assertEquals(FID_2, additionalMode2.get("SourceId.facility").textValue());
         Assertions.assertEquals(PID_2, additionalMode2.get("SourceId.patient").textValue());
      });

      for (int i = 0; i < JSON_CONFIG_1.auxGoldenRecordFields().size(); i++) {
         final String name = "GoldenRecord." + AppUtils.camelToSnake(JSON_CONFIG_1.auxGoldenRecordFields().get(i).fieldName());
         System.out.println(name);
         switch (JSON_CONFIG_1.auxGoldenRecordFields().get(i).fieldType()) {
            case "DateTime":
               final var dt = LocalDateTime.parse(dgraphGoldenRecordJsonNode.jsonNode().get(name).textValue());
               Assertions.assertEquals(DTF.format(NOW), DTF.format(dt));
               break;
            case "String":
               Assertions.assertEquals(AUX_FIELDS_1.get(i), dgraphGoldenRecordJsonNode.jsonNode().get(name).textValue());
               break;
            case "Bool":
               Assertions.assertEquals(Boolean.valueOf(AUX_FIELDS_1.get(i)),
                                       dgraphGoldenRecordJsonNode.jsonNode().get(name).booleanValue());
               break;
         }
      }

      for (int i = 0; i < JSON_CONFIG_1.demographicFields().size(); i++) {
         System.out.println("GoldenRecord." + AppUtils.camelToSnake(JSON_CONFIG_1.demographicFields().get(i).fieldName()));
         Assertions.assertEquals(DEMOGRAPHIC_FIELDS_1.get(i),
                                 dgraphGoldenRecordJsonNode.jsonNode()
                                                           .get("GoldenRecord." + AppUtils.camelToSnake(JSON_CONFIG_1
                                                           .demographicFields()
                                                                                                                     .get(i)
                                                                                                                     .fieldName
                                                                                                                     ()))
                                                           .textValue());
      }

   }
*/

}
