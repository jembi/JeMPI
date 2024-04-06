package org.jembi.jempi.shared.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.input.DeterministicRule;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.models.DemographicData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
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
   public final List<List<Operation>> deterministicLinkPrograms;
   public final List<List<Operation>> deterministicValidatePrograms;
   public final List<List<Operation>> deterministicMatchPrograms;

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

      deterministicLinkPrograms = generateDeterministicPrograms(jsonConfig,
                                                                jsonConfig.rules().link().deterministic());
      deterministicValidatePrograms = generateDeterministicPrograms(jsonConfig,
                                                                    jsonConfig.rules().validate().deterministic());
      deterministicMatchPrograms = generateDeterministicPrograms(jsonConfig,
                                                                 jsonConfig.rules().matchNotification().deterministic());

   }

   private static boolean isMatch(
         final String left,
         final String right) {
      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
   }

   private static void eq(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.left.get(arguments.field).value();
      final var r = arguments.right.get(arguments.field).value();
      evalStack.push(isMatch(l, r));
   }

   private static void switched(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var p1L = arguments.left.get(arguments.field).value();
      final var p1R = arguments.right.get(arguments.field).value();
      final var p2L = arguments.left.get(arguments.aux).value();
      final var p2R = arguments.right.get(arguments.aux).value();
      evalStack.push(isMatch(p1L, p1R) && isMatch(p2L, p2R)
                     || isMatch(p1L, p2R) && isMatch(p2L, p1R));
   }

   private static void match(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.left.get(arguments.field).value();
      final var r = arguments.right.get(arguments.field).value();
      evalStack.push(!StringUtils.isEmpty(l)
                     && !StringUtils.isEmpty(r)
                     && DISTANCE.apply(l, r) <= arguments.aux);
   }

   private static void isNull(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      evalStack.push(StringUtils.isEmpty(arguments.left.get(arguments.field).value()));
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

   private List<List<Operation>> generateDeterministicPrograms(
         final JsonConfig jsonConfig,
         final List<DeterministicRule> rules) {
      final List<List<Operation>> deterministicPrograms = new ArrayList<>();
      for (DeterministicRule deterministicRule : rules) {
         final var infix = Arrays.asList(deterministicRule.text().split(" "));
         final var rpn = shuntingYard(infix);
         LOGGER.debug("{}", deterministicRule.text());
         LOGGER.debug("{}", infix);
         LOGGER.debug("{}", rpn);
         final List<Operation> program = new ArrayList<>();
         var stackDepth = 0;
         for (final String s : rpn) {
            if (s.startsWith("eq")) {
               final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  program.add(new Operation(LinkerConfig::eq, fieldIndex, null));
                  stackDepth += 1;
               }
            } else if (s.startsWith("null")) {
               final var pattern = Pattern.compile("^null\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  program.add(new Operation(LinkerConfig::isNull, fieldIndex, null));
                  stackDepth += 1;
               }
            } else if (s.startsWith("match")) {
               final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var distance = Integer.valueOf(matcher.group("distance"));
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  program.add(new Operation(LinkerConfig::match, fieldIndex, distance));
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
                  program.add(new Operation(LinkerConfig::switched, field1Index, field2Index));
                  stackDepth += 1;
               }
            } else if (s.startsWith("and")) {
               program.add(new Operation(LinkerConfig::andOperator, null, null));
               stackDepth -= 1;
            } else if (s.startsWith("or")) {
               program.add(new Operation(LinkerConfig::orOperator, null, null));
               stackDepth -= 1;
            }
         }
         if (stackDepth != 1) {
            LOGGER.error("Stack Depth error: {}", stackDepth);
         } else {
            LOGGER.debug("Stack Depth {}", stackDepth);
            deterministicPrograms.add(program);
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
         List<DemographicData.Field> left,
         List<DemographicData.Field> right,
         Integer field,
         Integer aux) {
   }

   public record Operation(
         BiConsumer<Deque<Boolean>, Arguments> opcode,
         Integer field,
         Integer aux) {
   }

}
