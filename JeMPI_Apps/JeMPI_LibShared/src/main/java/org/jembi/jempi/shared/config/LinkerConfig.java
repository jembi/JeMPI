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
   public final List<List<Operation>> deterministicPrograms = new ArrayList<>();
   public final List<FieldProbabilisticMetaData> probabilisticLinkFields;
   public final List<FieldProbabilisticMetaData> probabilisticValidateFields;
   public final List<FieldProbabilisticMetaData> probabilisticMatchNotificationFields;


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

      for (DeterministicRule deterministicRule : jsonConfig.rules().link().deterministic()) {
         final var given = Arrays.asList(deterministicRule.text().split(" "));
         final var computed = shuntingYard(given);
         LOGGER.debug("{}", deterministicRule.text());
         LOGGER.debug("{}", given);
         LOGGER.debug("{}", computed);
         final List<Operation> program = new ArrayList<>();
         var stackDepth = 0;
         for (final String s : computed) {
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

   }

   static void eq(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.left.get(arguments.field).value();
      final var r = arguments.right.get(arguments.field).value();
      evalStack.push(!StringUtils.isEmpty(l)
                     && !StringUtils.isEmpty(r)
                     && l.equals(r));
   }

   static void switched(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var lIdx = arguments.left.get(arguments.field).value();
      final var rIdx = arguments.right.get(arguments.field).value();
      final var lP1 = arguments.left.get(arguments.aux).value();
      final var rP1 = arguments.right.get(arguments.aux).value();
      evalStack.push(!StringUtils.isEmpty(lIdx)
                     && !StringUtils.isEmpty(rIdx)
                     && !StringUtils.isEmpty(lP1)
                     && !StringUtils.isEmpty(rP1)
                     && ((lIdx.equals(rIdx) && lP1.equals(rP1)) || (lIdx.equals(rP1) && lP1.equals(rIdx))));
   }

   static void match(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = arguments.left.get(arguments.field).value();
      final var r = arguments.right.get(arguments.field).value();
      evalStack.push(!StringUtils.isEmpty(l)
                     && !StringUtils.isEmpty(r)
                     && DISTANCE.apply(l, r) <= arguments.aux);
   }

   static void isNull(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      evalStack.push(StringUtils.isEmpty(arguments.left.get(arguments.field).value()));
   }

   static void andOperator(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = evalStack.pop();
      final var r = evalStack.pop();
      evalStack.push(l && r);
   }

   static void orOperator(
         final Deque<Boolean> evalStack,
         final Arguments arguments) {
      final var l = evalStack.pop();
      final var r = evalStack.pop();
      evalStack.push(l || r);
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
