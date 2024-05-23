package org.jembi.jempi.shared.config.dgraph;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class DemographicDataFields {

   private static final Logger LOGGER = LogManager.getLogger(DemographicDataFields.class);

   private DemographicDataFields() {
   }

   public static List<Pair<String, Integer>> create(final JsonConfig jsonConfig) {
      final var configVar = new ArrayList<Pair<String, Integer>>();
      for (int i = 0; i < jsonConfig.demographicFields().size(); i++) {
         configVar.add(Pair.of(AppUtils.camelToSnake(jsonConfig.demographicFields().get(i).scFieldName()), i));
      }
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(configVar);
         LOGGER.info("{}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return configVar;
   }

}
