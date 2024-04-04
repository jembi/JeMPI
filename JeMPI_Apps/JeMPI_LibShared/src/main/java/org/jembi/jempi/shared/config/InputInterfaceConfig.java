package org.jembi.jempi.shared.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class InputInterfaceConfig {

   private static final Logger LOGGER = LogManager.getLogger(InputInterfaceConfig.class);
   public final List<Pair<String, Integer>> demographicDataCsvCols;

   InputInterfaceConfig(final JsonConfig jsonConfig) {
      demographicDataCsvCols =
            jsonConfig.demographicFields().stream().map(f -> Pair.of(f.fieldName(), f.source().csvCol())).toList();
      try {
         final var json = OBJECT_MAPPER.writeValueAsString(demographicDataCsvCols);
         LOGGER.info("{}", json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }
}
