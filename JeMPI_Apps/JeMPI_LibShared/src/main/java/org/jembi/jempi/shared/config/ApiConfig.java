package org.jembi.jempi.shared.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public class ApiConfig {

   private static final Logger LOGGER = LogManager.getLogger(ApiConfig.class);
   public final List<Pair<String, Integer>> demographicDataFields;

   ApiConfig(final JsonConfig jsonConfig) {
      demographicDataFields = new ArrayList<>();
      for (int i = 0; i < jsonConfig.demographicFields().size(); i++) {
         demographicDataFields.add(Pair.of(AppUtils.snakeToCamelCase(jsonConfig.demographicFields().get(i).fieldName()), i));
      }
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(demographicDataFields);
         LOGGER.info("{}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }


}
