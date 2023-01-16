package org.jembi.jempi.api;

import java.io.*;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonFieldsConfig {

    public JSONArray fields;

    private JSONArray concatArrays(JSONArray... arrays) {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrays) {
            for (int i = 0; i < arr.size(); i++) {
                result.add(arr.get(i));
            }
        }
        return result;
    }
    private InputStream getFileStreamFromResource() {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream("/config-reference.json");
    }

    public void load() {
        JSONParser jsonParser = new JSONParser();
        try (Reader reader = new InputStreamReader(getFileStreamFromResource()))
        {
            // Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject config = (JSONObject) obj;
            JSONArray systemFields = (JSONArray) config.get("systemFields");
            JSONArray customFields = (JSONArray) config.get("fields");
            fields = concatArrays(systemFields, customFields);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
