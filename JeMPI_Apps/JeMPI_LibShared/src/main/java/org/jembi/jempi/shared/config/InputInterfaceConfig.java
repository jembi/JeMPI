package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InputInterfaceConfig {

   public final List<Pair<String, Source>> demographicDataSource;
   public final Map<String, Source> auxInteractionDataSource = new HashMap<>();
   public final Map<String, Map<String, Source>> additionalNodesSource = new HashMap<>();
   public final Integer auxIdCsvCol;
   public final Integer auxClinicalDataCsvCol;
   public final Integer sourceIdPatientCsvCol;
   public final Integer sourceIdFacilityCsvCol;

   InputInterfaceConfig(final JsonConfig jsonConfig) {
      demographicDataSource = jsonConfig.demographicFields().stream().map(f -> Pair.of(f.fieldName(), f.source())).toList();
      jsonConfig.auxInteractionFields()
                .stream()
                .filter(field -> field.source() != null)
                .forEach(field -> auxInteractionDataSource.put(field.fieldName(), field.source()));
      jsonConfig.additionalNodes().forEach(node -> {
         final var map = new HashMap<String, Source>();
         node.fields().forEach(field -> map.put(field.fieldName(), field.source()));
         additionalNodesSource.put(node.nodeName(), map);
      });

      auxIdCsvCol = auxInteractionDataSource.get("aux_id").csvCol();
      auxClinicalDataCsvCol = auxInteractionDataSource.get("aux_clinical_data").csvCol();
      sourceIdFacilityCsvCol = additionalNodesSource.get("SourceId").get("facility").csvCol();
      sourceIdPatientCsvCol = additionalNodesSource.get("SourceId").get("patient").csvCol();
   }
}
