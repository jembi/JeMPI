package org.jembi.jempi.shared.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.linker.Programs;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The type Linker config.
 */
public final class LinkerConfig {

   private static final Logger LOGGER = LogManager.getLogger(LinkerConfig.class);
   /**
    * The Probabilistic link fields.
    */
   public final List<FieldProbabilisticMetaData> probabilisticLinkFields;
   /**
    * The Probabilistic validate fields.
    */
   public final List<FieldProbabilisticMetaData> probabilisticValidateFields;
   /**
    * The Probabilistic match notification fields.
    */
   public final List<FieldProbabilisticMetaData> probabilisticMatchNotificationFields;
   /**
    * The Deterministic link programs. Pair(link, canApplyLink)
    */
   public final List<Programs.DeterministicProgram> deterministicLinkPrograms;
   /**
    * The Deterministic validate programs.
    */
   public final List<Programs.DeterministicProgram> deterministicValidatePrograms;
   /**
    * The Deterministic match programs.
    */
   public final List<Programs.DeterministicProgram> deterministicMatchPrograms;

   /**
    * The Block link programs.
    */
   public final List<Programs.BlockProgram> blockLinkPrograms;

   /**
    * The Block match programs.
    */
   public final List<Programs.BlockProgram> blockMatchPrograms;

   /**
    * Instantiates a new Linker config.
    *
    * @param jsonConfig the json config
    */
   LinkerConfig(final JsonConfig jsonConfig) {

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

      LOGGER.debug("generate programs");
      if ((jsonConfig.rules().link() != null) && !AppUtils.isNullOrEmpty(jsonConfig.rules().link().deterministic())) {
         deterministicLinkPrograms = Programs.generateDeterministicPrograms("link", jsonConfig,
                                                                            jsonConfig.rules().link().deterministic());
      } else {
         deterministicLinkPrograms = new ArrayList<>();
      }

      if (jsonConfig.rules().validate() != null && !AppUtils.isNullOrEmpty(jsonConfig.rules().validate().deterministic())) {
         deterministicValidatePrograms = Programs.generateDeterministicPrograms("validate", jsonConfig,
                                                                                jsonConfig.rules().validate().deterministic());
      } else {
         deterministicValidatePrograms = new ArrayList<>();
      }
      if (jsonConfig.rules().matchNotification() != null && !AppUtils.isNullOrEmpty(jsonConfig.rules()
                                                                                              .matchNotification()
                                                                                              .deterministic())) {
         deterministicMatchPrograms = Programs.generateDeterministicPrograms("match", jsonConfig,
                                                                             jsonConfig.rules()
                                                                                       .matchNotification()
                                                                                       .deterministic());
      } else {
         deterministicMatchPrograms = new ArrayList<>();
      }
      if (jsonConfig.rules().link() != null && !AppUtils.isNullOrEmpty(jsonConfig.rules().link().probabilistic())) {
         blockLinkPrograms = Programs.generateBlockPrograms("link", jsonConfig,
                                                            jsonConfig.rules().link().probabilistic());
      } else {
         blockLinkPrograms = new ArrayList<>();
      }
      if (jsonConfig.rules().matchNotification() != null && !AppUtils.isNullOrEmpty(jsonConfig.rules().matchNotification()
                                                                                              .probabilistic())) {
         blockMatchPrograms = Programs.generateBlockPrograms("match", jsonConfig,
                                                             jsonConfig.rules().matchNotification().probabilistic());
      } else {
         blockMatchPrograms = new ArrayList<>();
      }
      LOGGER.debug("generated programs");

   }


   /**
    * The type Field probabilistic metadata.
    */
   public record FieldProbabilisticMetaData(
         Integer demographicDataIndex,
         String similarityScore,
         List<Float> comparisonLevels,
         Float m,
         Float u) {
   }


}
