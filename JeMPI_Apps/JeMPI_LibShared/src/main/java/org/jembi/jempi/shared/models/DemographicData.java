package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.API_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

/**
 * The type Demographic data.
 */
public class DemographicData {

   /**
    * The Fields.
    */
   public final List<DemographicData.DemographicField> fields;

   /**
    * Instantiates a new Demographic data.
    */
   public DemographicData() {
      fields = new ArrayList<>();
   }

   /**
    * Instantiates a new Demographic data.
    *
    * @param demographicData the demographic data
    */
   public DemographicData(final DemographicData demographicData) {
      fields = demographicData.fields.stream().toList();
   }

   /**
    * Instantiates a new Demographic data.
    *
    * @param fields the fields
    */
   public DemographicData(final List<DemographicData.DemographicField> fields) {
      this.fields = fields;
   }

   /**
    * From demographic data json node.
    *
    * @param demographicData the demographic data
    * @return the json node
    */
   public static JsonNode fromDemographicData(final DemographicData demographicData) {
      final var objectNode = OBJECT_MAPPER.createObjectNode();
      demographicData.fields.forEach(field -> {
         if (field.ccTag != null && !field.ccTag.isEmpty()) {
            objectNode.put(field.ccTag, field.value);
         }
      });
      return objectNode;
   }

   /**
    * From custom demographic data demographic data.
    *
    * @param jsonNode the json node
    * @return the demographic data
    */
   public static DemographicData fromCustomDemographicData(final JsonNode jsonNode) {
      final var fields = new ArrayList<DemographicField>();
      API_CONFIG.demographicDataFields.forEach(field -> {
         final var val = jsonNode.get(field.getLeft()).textValue();
         fields.add(field.getRight(), new DemographicField(field.getLeft(), val));
      });
      return new DemographicData(fields);
   }

   /**
    * Gets aliases.
    *
    * @return the aliases
    */
   public static String getAliases() {
      final ArrayList<String> names = new ArrayList<>();
      API_CONFIG.demographicDataFields.forEach(field -> names.add(field.getRight(), field.getLeft()));
      return StringUtils.join(names, ",");

   }

   /**
    * Get alias array string [ ].
    *
    * @return the string [ ]
    */
   public static String[] getAliasArray() {
      return API_CONFIG.demographicDataFields.stream()
                                             .map(Pair::getLeft)
                                             .toList()
                                             .toArray(new String[API_CONFIG.demographicDataFields.size()]);
   }

   /**
    * Clean demographic data.
    *
    * @return the demographic data
    */
   public DemographicData clean() {
      return new DemographicData(
            fields.stream()
                  .map(x -> new DemographicData.DemographicField(x.ccTag,
                                                                 x.value.trim()
                                                                        .toLowerCase()
                                                                        .replaceAll("\\W", "")))
                  .toList());
   }

   /**
    * The type Demographic field.
    */
   public record DemographicField(
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }

}
