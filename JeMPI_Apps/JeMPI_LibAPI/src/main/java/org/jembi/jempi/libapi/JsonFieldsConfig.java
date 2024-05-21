package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class JsonFieldsConfig {

   private static final Logger LOGGER = LogManager.getLogger(JsonFieldsConfig.class);
   public String jsonFields;
   private JSONArray fields;

   public JsonFieldsConfig(final String resourceFilename) {
      try {
         load(resourceFilename);
      } catch (Exception e) {
         LOGGER.debug(e);
      }
   }

   private String snakeToCamelCase(final String str) {
      String[] words = str.split("_");
      String result = words[0];
      for (int i = 1; i < words.length; i++) {
         result += words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
      }
      return result;
   }

   private JSONArray buildFieldsResponsePayload(
         final JSONArray systemFields,
         final JSONArray customFields) {
      JSONArray result = new JSONArray();
      // Process system fields
      for (Object systemField : systemFields) {
         JSONObject field = (JSONObject) systemField;
         // Mark field as readonly
         field.put("readOnly", true);
         // Merge array values
         result.add(field);
      }
      // Process custom fields
      for (Object customField : customFields) {
         // Convert field names from snake case to camel case
         JSONObject field = (JSONObject) customField;
         String fieldName = (String) field.get("fieldName");
         field.put("fieldName", snakeToCamelCase(fieldName));
         // Remove extra attributes
         field.remove("indexGoldenRecord");
         field.remove("indexPatient");
         field.remove("m");
         field.remove("u");
         // Mark field as editable
         field.put("readOnly", false);
         // Merge array values
         result.add(field);
      }
      return result;
   }

   private InputStream getFileStreamFromResource(final String resourceFilename) {
      ClassLoader classLoader = getClass().getClassLoader();
      return classLoader.getResourceAsStream(resourceFilename);
   }

   public void load(final String resourceFilename) {
      JSONParser jsonParser = new JSONParser();
      try (Reader reader = new InputStreamReader(getFileStreamFromResource(resourceFilename))) {
         // Read JSON file
         Object obj = jsonParser.parse(reader);
         JSONObject config = (JSONObject) obj;
         // System fields are fields that exists regardless of the implementation
         JSONArray systemFields = (JSONArray) config.get("systemFields");
         // Custom fields depend on the needs of the implementation
         JSONArray customFields = (JSONArray) config.get("fields");
         jsonFields = buildFieldsResponsePayload(systemFields, customFields).toJSONString();
      } catch (ParseException | IOException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         fields = new JSONArray();
      }
   }
}
