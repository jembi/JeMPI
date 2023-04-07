package org.jembi.jempi.shared.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class JsonFieldsConfig {

   public JSONArray fields;

   public JsonFieldsConfig() {
      try {
         load();
      } catch (Exception e) {
         LOGGER.debug(e);
      }
   }

   private static final Logger LOGGER = LogManager.getLogger(JsonFieldsConfig.class);
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
      for (int i = 0; i < systemFields.size(); i++) {
         JSONObject field = (JSONObject) systemFields.get(i);
         // Mark field as readonly
         field.put("readOnly", true);
         // Merge array values
         result.add(field);
      }
      // Process custom fields
      for (int i = 0; i < customFields.size(); i++) {
         // Convert field names from snake case to camel case
         JSONObject field = (JSONObject) customFields.get(i);
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

   private InputStream getFileStreamFromResource() {
      ClassLoader classLoader = getClass().getClassLoader();
      return classLoader.getResourceAsStream("/config-reference.json");
   }

   public void load() throws Exception {
      JSONParser jsonParser = new JSONParser();
      try (Reader reader = new InputStreamReader(getFileStreamFromResource())) {
         // Read JSON file
         Object obj = jsonParser.parse(reader);
         JSONObject config = (JSONObject) obj;
         // System fields are fields that exists regardless of the implementation
         JSONArray systemFields = (JSONArray) config.get("systemFields");
         // Custom fields depend on the needs of the implementation
         JSONArray customFields = (JSONArray) config.get("fields");
         fields = buildFieldsResponsePayload(systemFields, customFields);

      } catch (FileNotFoundException e) {
         throw e;
      }
   }
}
