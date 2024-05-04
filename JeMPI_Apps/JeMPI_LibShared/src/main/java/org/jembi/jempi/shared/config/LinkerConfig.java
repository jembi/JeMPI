package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.DeterministicRule;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.input.ShuntingYard.shuntingYard;

public final class LinkerConfig {

   private static final LevenshteinDistance DISTANCE = LevenshteinDistance.getDefaultInstance();
   private static final Logger LOGGER = LogManager.getLogger(LinkerConfig.class);
   public final List<FieldProbabilisticMetaData> probabilisticLinkFields;
   public final List<FieldProbabilisticMetaData> probabilisticValidateFields;
   public final List<FieldProbabilisticMetaData> probabilisticMatchNotificationFields;
   public final List<Pair<List<Operation>, List<Operation>>> deterministicLinkPrograms;
   public final List<Pair<List<Operation>, List<Operation>>> deterministicValidatePrograms;
   public final List<Pair<List<Operation>, List<Operation>>> deterministicMatchPrograms;

   LinkerConfig(final JsonConfig jsonConfig) {

      probabilisticLinkFields = IntStream
            .range(0, jsonConfig.demographicFields().size())
            .filter(i -> jsonConfig.demographicFields().get(i).linkMetaData() != null)
            .mapToObj(i -> {
               final var metadata = jsonConfig.demographicFields().get(i).linkMetaData();
               return new LinkerConfig.FieldProbabilisticMetaData(i,
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
      if ((jsonConfig.rules().link() != null)
          && !AppUtils.isNullOrEmpty(jsonConfig.rules().link().deterministic())) {
         deterministicLinkPrograms = generateDeterministicPrograms(jsonConfig,
                                                                   jsonConfig.rules().link().deterministic());
      } else {
         deterministicLinkPrograms = new ArrayList<>();
      }

      if (jsonConfig.rules().validate() != null
          && !AppUtils.isNullOrEmpty(jsonConfig.rules().validate().deterministic())) {
         deterministicValidatePrograms = generateDeterministicPrograms(jsonConfig,
                                                                       jsonConfig.rules().validate().deterministic());
      } else {
         deterministicValidatePrograms = new ArrayList<>();
      }
      if (jsonConfig.rules().matchNotification() != null
          && !AppUtils.isNullOrEmpty(jsonConfig.rules().matchNotification().deterministic())) {
         deterministicMatchPrograms = generateDeterministicPrograms(jsonConfig,
                                                                    jsonConfig.rules().matchNotification().deterministic());
      } else {
         deterministicMatchPrograms = new ArrayList<>();
      }
      LOGGER.debug("generated programs");

   }

   private static void interactionFieldNotBlank(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.interaction.get(arguments.field).value();
      evalStack.push(StringUtils.isNotBlank(l));
   }

   private static void interactionFieldIsBlank(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.interaction.get(arguments.field).value();
      evalStack.push(!StringUtils.isNotBlank(l));
   }

   private static boolean isMatch(
         final String left,
         final String right) {
      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
   }

   private static void eq(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.interaction.get(arguments.field).value();
      final var r = arguments.goldenRecord.get(arguments.field).value();
      evalStack.push(isMatch(l, r));
   }

   private static void switched(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var p1L = arguments.interaction.get(arguments.field).value();
      final var p1R = arguments.goldenRecord.get(arguments.field).value();
      final var p2L = arguments.interaction.get(arguments.aux).value();
      final var p2R = arguments.goldenRecord.get(arguments.aux).value();
      evalStack.push(isMatch(p1L, p1R) && isMatch(p2L, p2R)
                     || isMatch(p1L, p2R) && isMatch(p2L, p1R));
   }

   private static void match(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.interaction.get(arguments.field).value();
      final var r = arguments.goldenRecord.get(arguments.field).value();
      evalStack.push(!StringUtils.isEmpty(l)
                     && !StringUtils.isEmpty(r)
                     && DISTANCE.apply(l, r) <= arguments.aux);
   }

   private static void isNull(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      evalStack.push(StringUtils.isEmpty(arguments.interaction.get(arguments.field).value()));
   }

   private static void andOperator(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = evalStack.pop();
      final var r = evalStack.pop();
      evalStack.push(l && r);
   }

   private static void orOperator(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = evalStack.pop();
      final var r = evalStack.pop();
      evalStack.push(l || r);
   }

   public boolean canApplyLinking(
         final List<Pair<List<Operation>, List<LinkerConfig.Operation>>> programs,
         final DemographicData interaction) {
      if (!probabilisticLinkFields.isEmpty()) {
         return true;
      }
      for (final var program : programs) {
         final Deque<Boolean> evalStack = new ArrayDeque<>();
         for (final var operation : program.getRight()) {
            operation.opcode()
                     .accept(evalStack,
                             new LinkerConfig.Arguments(interaction.fields, null, operation.field(), operation.aux()));
         }
         if (Boolean.TRUE.equals(evalStack.pop())) {
            return true;
         }
      }
      return false;
   }

   public static boolean runDeterministicPrograms(
         final List<Pair<List<LinkerConfig.Operation>, List<LinkerConfig.Operation>>> programs,
         final DemographicData interaction,
         final DemographicData goldenRecord) {
      for (final var program : programs) {
         final Deque<Boolean> evalStack = new ArrayDeque<>();
         for (final var operation : program.getLeft()) {
            operation.opcode()
                     .accept(evalStack,
                             new LinkerConfig.Arguments(interaction.fields,
                                                        goldenRecord.fields,
                                                        operation.field(),
                                                        operation.aux()));
         }
         if (Boolean.TRUE.equals(evalStack.pop())) {
            return true;
         }
      }
      return false;
   }

   private List<Pair<List<Operation>, List<Operation>>> generateDeterministicPrograms(
         final JsonConfig jsonConfig,
         final List<DeterministicRule> rules) {
      final List<Pair<List<Operation>, List<Operation>>> deterministicPrograms = new ArrayList<>();
      if (rules.isEmpty()) {
         return deterministicPrograms;
      }
      for (DeterministicRule deterministicRule : rules) {
         final var infix = Arrays.asList(deterministicRule.text().split(" "));
         final var rpn = shuntingYard(infix);
         LOGGER.debug("{}", deterministicRule.text());
         LOGGER.debug("{}", infix);
         LOGGER.debug("{}", rpn);
         final List<Operation> canApplyProgram = new ArrayList<>();
         final List<Operation> program = new ArrayList<>();
         var stackDepth = 0;
         for (final String s : rpn) {
            if (s.startsWith("eq")) {
               final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(LinkerConfig::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(LinkerConfig::eq, fieldIndex, null));
                  stackDepth += 1;
               }
            } else if (s.startsWith("match")) {
               final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var distance = Integer.valueOf(matcher.group("distance"));
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(LinkerConfig::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(LinkerConfig::match, fieldIndex, distance));
                  stackDepth += 1;
               }
            } else if (s.startsWith("isNull")) {
               final var pattern = Pattern.compile("^isNull\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(LinkerConfig::interactionFieldIsBlank, fieldIndex, null));
                  program.add(new Operation(LinkerConfig::isNull, fieldIndex, null));
                  stackDepth += 1;
               }
            } else if (s.startsWith("switched")) {
               final var pattern = Pattern.compile("^switched\\((?<field1>\\w+),(?<field2>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field1 = matcher.group("field1");
                  final var field2 = matcher.group("field2");
                  final var field1Index = fieldIndexOf(jsonConfig, field1);
                  final var field2Index = fieldIndexOf(jsonConfig, field2);
                  canApplyProgram.add(new Operation(LinkerConfig::interactionFieldNotBlank, field1Index, null));
                  program.add(new Operation(LinkerConfig::switched, field1Index, field2Index));
                  stackDepth += 1;
               }
            } else if (s.startsWith("and")) {
               canApplyProgram.add(new Operation(LinkerConfig::andOperator, null, null));
               program.add(new Operation(LinkerConfig::andOperator, null, null));
               stackDepth -= 1;
            } else if (s.startsWith("or")) {
               canApplyProgram.add(new Operation(LinkerConfig::orOperator, null, null));
               program.add(new Operation(LinkerConfig::orOperator, null, null));
               stackDepth -= 1;
            }
         }
         if (stackDepth != 1) {
            LOGGER.error("Stack Depth error: {}", stackDepth);
         } else {
            LOGGER.debug("Stack Depth {}", stackDepth);
            deterministicPrograms.add(Pair.of(program, canApplyProgram));
         }
      }
      return deterministicPrograms;
   }

   private int fieldIndexOf(
         final JsonConfig jsonConfig,
         final String field) {
      for (int f = 0; f < jsonConfig.demographicFields().size(); f++) {
         if (jsonConfig.demographicFields().get(f).fieldName().equals(field)) {
            return f;
         }
      }
      return -1;
   }

   public record FieldProbabilisticMetaData(
         Integer demographicDataIndex,
         String similarityScore,
         List<Float> comparisonLevels,
         Float m,
         Float u) {
   }

   public record Arguments(
         List<DemographicData.DemographicField> interaction,
         List<DemographicData.DemographicField> goldenRecord,
         Integer field,
         Integer aux) {
   }

   public record Operation(
         BiConsumer<Deque<Boolean>, Arguments> opcode,
         Integer field,
         Integer aux) {
   }

}
