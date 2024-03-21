package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jembi.jempi.shared.config.input.JsonConfig;

import java.util.List;
import java.util.stream.IntStream;

public final class Config {

   private static final JsonConfig JSON_CONFIGURATION = JsonConfig.fromJson("config-reference.json");
// private static final JsonConfig JSON_CONFIGURATION = JsonConfig.fromJson("config-reference-link-d-validate-dp-match-dp.json");
   public static final InputInterface INPUT_INTERFACE = new InputInterface(JSON_CONFIGURATION);
   public static final Linker LINKER = new Linker(JSON_CONFIGURATION);

   private Config() {
   }

   public static final class InputInterface {
      public final List<Pair<String, Integer>> demographicDataCsvCols;

      private InputInterface(final JsonConfig jsonConfig) {
         demographicDataCsvCols =
               jsonConfig.demographicFields().stream().map(f -> Pair.of(f.fieldName(), f.source().csvCol())).toList();
      }
   }

   public static final class Linker {
      public final List<FieldProbabilisticMetaData> probabilisticLinkFields;
      public final List<FieldProbabilisticMetaData> probabilisticValidateFields;
      public final List<FieldProbabilisticMetaData> probabilisticMatchNotificationFields;

      private Linker(final JsonConfig jsonConfig) {
         probabilisticLinkFields = IntStream
               .range(0, jsonConfig.demographicFields().size())
               .filter(i -> jsonConfig.demographicFields().get(i).linkMetaData() != null)
               .mapToObj(i -> {
                  final var metadata = jsonConfig.demographicFields().get(i).linkMetaData();
                  return new FieldProbabilisticMetaData(i,
                                                        metadata.comparison(),
                                                        metadata.comparisonLevels(),
                                                        metadata.m(),
                                                        metadata.u());
               })
               .toList();
         probabilisticValidateFields = IntStream
               .range(0, jsonConfig.demographicFields().size())
               .filter(i -> jsonConfig.demographicFields().get(i).validateMetaData() != null)
               .mapToObj(i -> {
                  final var metadata = jsonConfig.demographicFields().get(i).validateMetaData();
                  return new FieldProbabilisticMetaData(i,
                                                        metadata.comparison(),
                                                        metadata.comparisonLevels(),
                                                        metadata.m(),
                                                        metadata.u());
               })
               .toList();
         probabilisticMatchNotificationFields = IntStream
               .range(0, jsonConfig.demographicFields().size())
               .filter(i -> jsonConfig.demographicFields().get(i).matchMetaData() != null)
               .mapToObj(i -> {
                  final var metadata = jsonConfig.demographicFields().get(i).matchMetaData();
                  return new FieldProbabilisticMetaData(i,
                                                        metadata.comparison(),
                                                        metadata.comparisonLevels(),
                                                        metadata.m(),
                                                        metadata.u());
               })
               .toList();
      }

      public record FieldProbabilisticMetaData(
            Integer demographicDataIndex,
            String similarityScore,
            List<Float> comparisonLevels,
            Float m,
            Float u) {
      }
   }

}
