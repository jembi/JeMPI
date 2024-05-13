package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.config.Config.API_CONFIG;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public class DemographicData {

   public final List<DemographicData.DemographicField> fields;

   public DemographicData() {
      fields = new ArrayList<>();
   }

   public DemographicData(final DemographicData demographicData) {
      fields = demographicData.fields.stream().toList();
   }

   public DemographicData(final List<DemographicData.DemographicField> fields) {
      this.fields = fields;
   }

   public static JsonNode fromDemographicData(final DemographicData demographicData) {
      final var objectNode = OBJECT_MAPPER.createObjectNode();
      demographicData.fields.forEach(field -> {
         if (field.ccTag != null && !field.ccTag.isEmpty()) {
            objectNode.put(field.ccTag, field.value);
         }
      });
      return objectNode;
   }

   public static DemographicData fromCustomDemographicData(final JsonNode jsonNode) {
      final var fields = new ArrayList<DemographicField>();
      API_CONFIG.demographicDataFields.forEach(field -> {
         final var val = jsonNode.get(field.getLeft()).textValue();
         fields.add(field.getRight(), new DemographicField(field.getLeft(), val));
      });
      return new DemographicData(fields);
   }

   public static String getAliases() {
      final ArrayList<String> names = new ArrayList<>();
      API_CONFIG.demographicDataFields.forEach(field -> names.add(field.getRight(), field.getLeft()));
      return StringUtils.join(names, ",");

   }

   public static String[] getAliasArray() {
      return API_CONFIG.demographicDataFields.stream()
                                             .map(Pair::getLeft)
                                             .toList()
                                             .toArray(new String[API_CONFIG.demographicDataFields.size()]);
   }

   public DemographicData clean() {
      return new DemographicData(
            fields.stream()
                  .map(x -> new DemographicData.DemographicField(x.ccTag,
                                                                 x.value.trim()
                                                                        .toLowerCase()
                                                                        .replaceAll("\\W", "")))
                  .toList());
   }

   public record DemographicField(
         @JsonProperty("tag") String ccTag,
         @JsonProperty("value") String value) {
   }

}
