package org.jembi.jempi.shared.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;

public final class Config {

   public static final String CONFIG_FILE = "config-reference.json";
   // public static final String CONFIG_FILE = "config-reference-link-d-validate-dp-match-dp.json";
   public static final InputInterface INPUT_INTERFACE;
   public static final LinkerConfig LINKER;
   private static final Logger LOGGER = LogManager.getLogger(Config.class);

   static {
      final var jsonConfig = JsonConfig.fromJson(CONFIG_FILE);
      INPUT_INTERFACE = new InputInterface(jsonConfig);
      LINKER = new LinkerConfig(jsonConfig);
   }

   private Config() {
   }

/*
   public static final class InputInterface {
      public final List<Pair<String, Integer>> demographicDataCsvCols;

      private InputInterface(final JsonConfig jsonConfig) {
         demographicDataCsvCols =
               jsonConfig.demographicFields().stream().map(f -> Pair.of(f.fieldName(), f.source().csvCol())).toList();
         try {
            LOGGER.info("{}", OBJECT_MAPPER.writeValueAsString(demographicDataCsvCols));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      }
   }
*/

/*
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
*/

}
