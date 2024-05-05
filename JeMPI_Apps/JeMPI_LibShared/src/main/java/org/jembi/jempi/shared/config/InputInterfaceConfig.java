package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InputInterfaceConfig {

   private static final Logger LOGGER = LogManager.getLogger(InputInterfaceConfig.class);
   public final List<Pair<String, Integer>> demographicDataCsvCols;
   public final Map<String, Integer> auxInteractionDataCsvCols = new HashMap<>();
   public final Map<String, Map<String, Integer>> additionalNodesCsvCols = new HashMap<>();
   public final Integer auxIdCsvCol;
   public final Integer auxDateCreatedCsvCol;
   public final Integer auxClinicalDataCsvCol;
   public final Integer sourceIdPatientCsvCol;
   public final Integer sourceIdFacilityCsvCol;


   InputInterfaceConfig(final JsonConfig jsonConfig) {
      demographicDataCsvCols = jsonConfig.demographicFields()
                                         .stream()
                                         .map(f -> Pair.of(f.fieldName(), f.source().csvCol()))
                                         .toList();
      jsonConfig.auxInteractionFields().forEach(field -> auxInteractionDataCsvCols.put(field.fieldName(), field.csvCol()));
      jsonConfig.additionalNodes().forEach(node -> {
         final var map = new HashMap<String, Integer>();
         node.fields().forEach(field -> map.put(field.fieldName(), field.csvCol()));
         additionalNodesCsvCols.put(node.nodeName(), map);
      });

      auxDateCreatedCsvCol = auxInteractionDataCsvCols.get("aux_date_created");
      auxIdCsvCol = auxInteractionDataCsvCols.get("aux_id");
      auxClinicalDataCsvCol = auxInteractionDataCsvCols.get("aux_clinical_data");
      sourceIdFacilityCsvCol = additionalNodesCsvCols.get("SourceId").get("facility");
      sourceIdPatientCsvCol = additionalNodesCsvCols.get("SourceId").get("patient");
   }
}
