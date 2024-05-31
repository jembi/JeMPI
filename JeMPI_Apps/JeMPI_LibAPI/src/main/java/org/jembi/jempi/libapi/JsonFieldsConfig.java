package org.jembi.jempi.libapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.utils.AppUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.FileSystems;

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
         field.put("fieldName", AppUtils.snakeToCamelCase(fieldName));
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

   public void load(final String filename) {
      final var separator = FileSystems.getDefault().getSeparator();
      final var filePath = "%sapp%sconf_system%s%s".formatted(separator, separator, separator, filename);
      final var file = new File(filePath);
      try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
         JSONParser jsonParser = new JSONParser();
         Object obj = jsonParser.parse(reader);
         JSONObject config = (JSONObject) obj;
         // System fields are fields that exists regardless of the implementation
         JSONArray systemFields = (JSONArray) config.get("systemFields");
         // Custom fields depend on the needs of the implementation
         JSONArray customFields = (JSONArray) config.get("fields");
         jsonFields = buildFieldsResponsePayload(systemFields, customFields).toJSONString();
      } catch (IOException | ParseException e) {
         throw new RuntimeException(e);
      }

//
//      JSONParser jsonParser = new JSONParser();
//      try (Reader reader = new InputStreamReader(getFileStreamFromResource(resourceFilename))) {
//         // Read JSON file
//         Object obj = jsonParser.parse(reader);
//         JSONObject config = (JSONObject) obj;
//         // System fields are fields that exists regardless of the implementation
//         JSONArray systemFields = (JSONArray) config.get("systemFields");
//         // Custom fields depend on the needs of the implementation
//         JSONArray customFields = (JSONArray) config.get("fields");
//         jsonFields = buildFieldsResponsePayload(systemFields, customFields).toJSONString();
//      } catch (ParseException | IOException e) {
//         LOGGER.error(e.getLocalizedMessage(), e);
//         fields = new JSONArray();
//      }
   }
}
