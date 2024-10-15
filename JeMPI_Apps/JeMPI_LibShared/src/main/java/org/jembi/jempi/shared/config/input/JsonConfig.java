package org.jembi.jempi.shared.config.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonConfig(
      @JsonProperty("auxInteractionFields") List<AuxInteractionField> auxInteractionFields,
      @JsonProperty("auxGoldenRecordFields") List<AuxGoldenRecordField> auxGoldenRecordFields,
      List<AdditionalNode> additionalNodes,
      List<DemographicField> demographicFields,
      @JsonProperty("fieldsForKafkaKeyGen") List<String> fieldsForKafkaKeyGen,
      @JsonProperty("nameFieldsForNotificationDisplay") List<String> nameFieldsForNotificationDisplay,
      Rules rules) {

   private static final Logger LOGGER = LogManager.getLogger(JsonConfig.class);

   public static JsonConfig fromJson(final String filePath) {
      JsonConfig cfg;
      final var file = new File(filePath);
      try (InputStream in = new FileInputStream(file)) {
         cfg = OBJECT_MAPPER.readValue(in, JsonConfig.class);
      } catch (IOException e) {
         LOGGER.error(filePath);
         LOGGER.error(e.getLocalizedMessage(), e);
         cfg = null;
      }
      return cfg;
   }
}
