package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.InputStream;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonConfig(
      @JsonProperty("uniqueInteractionFields") List<AuxInteractionField> auxInteractionFields,
      @JsonProperty("uniqueGoldenRecordFields") List<AuxGoldenRecordField> auxGoldenRecordFields,
      List<AdditionalNode> additionalNodes,
      List<DemographicField> demographicFields,
      Rules rules) {

   public static JsonConfig fromJson(final String jsonFile) {
      JsonConfig cfg;
      try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(jsonFile)) {
         cfg = OBJECT_MAPPER.readValue(in, JsonConfig.class);
      } catch (Exception e) {
         cfg = null;
      }
      return cfg;
   }
}