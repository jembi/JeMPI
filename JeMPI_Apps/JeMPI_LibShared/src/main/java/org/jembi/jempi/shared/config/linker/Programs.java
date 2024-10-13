package org.jembi.jempi.shared.config.linker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.LinkerConfig;
import org.jembi.jempi.shared.config.input.DeterministicRule;
import org.jembi.jempi.shared.config.input.JsonConfig;
import org.jembi.jempi.shared.config.input.ProbabilisticRule;
import org.jembi.jempi.shared.models.DemographicData;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jembi.jempi.shared.config.input.ShuntingYard.shuntingYard;

/**
 * The type Programs.
 */
public final class Programs {

   private static final Logger LOGGER = LogManager.getLogger(Programs.class);
   private static final LevenshteinDistance DISTANCE = LevenshteinDistance.getDefaultInstance();

   private Programs() {
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

   private static String postfixToInfix1(
         final List<String> vars,
         final List<String> postfix) {
      final Deque<String> evalStack = new ArrayDeque<>();

      for (final String s : postfix) {
         if (s.startsWith("eq(")) {
            final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
            final var matcher = pattern.matcher(s);
            if (matcher.find()) {
               final var field = matcher.group("field");
               int i = 0;
               while (i < vars.size() && !field.equals(vars.get(i))) {
                  i++;
               }
               evalStack.push("uid(%c)".formatted('A' + i));
            }
         } else if (s.startsWith("match(")) {
            final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
            final var matcher = pattern.matcher(s);
            if (matcher.find()) {
               final var field = matcher.group("field");
//               final var distance = Integer.valueOf(matcher.group("distance"));
               int i = 0;
               while (i < vars.size() && !field.equals(vars.get(i))) {
                  i++;
               }
               evalStack.push("uid(%c)".formatted('A' + i));
            }
         } else if (s.startsWith("null(")) {
            final var pattern = Pattern.compile("^null\\((?<field>\\w+)\\)$");
            final var matcher = pattern.matcher(s);
            if (matcher.find()) {
               final var field = matcher.group("field");
               int i = 0;
               while (i < vars.size() && !field.equals(vars.get(i))) {
                  i++;
               }
               evalStack.push("uid(%c)".formatted('A' + i));
            }
         } else {
            final var operand1 = evalStack.pop();
            final var operand2 = evalStack.pop();
            evalStack.push("(" + operand2 + " %s ".formatted(s.toUpperCase()) + operand1 + ")");
         }
      }
      return evalStack.pop();
   }

   private static String postfixToInfix2(
         final List<CompileMetaData> meta,
         final List<String> postfix) {
      final Deque<String> evalStack = new ArrayDeque<>();

      for (final String s : postfix) {
         if (s.startsWith("eq(")) {
            final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
            final var matcher = pattern.matcher(s);
            if (matcher.find()) {
               final var field = matcher.group("field");
               int i = 0;
               while (i < meta.size() && !field.equals(meta.get(i).alias)) {
                  i++;
               }
               evalStack.push("uid(%c)".formatted('A' + i));
            }
         } else if (s.startsWith("match(")) {
            final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
            final var matcher = pattern.matcher(s);
            if (matcher.find()) {
               final var field = matcher.group("field");
               final var distance = Integer.valueOf(matcher.group("distance"));
               int i = 0;
               while (i < meta.size() && !(AppUtils.snakeToCamelCase("match_%d_%s".formatted(distance, field))
                                                   .equals(meta.get(i).alias))) {
                  i++;
               }
               evalStack.push("uid(%s)".formatted(meta.get(i).alias));
            }
         } else {
            final var operand1 = evalStack.pop();
            final var operand2 = evalStack.pop();
            evalStack.push("(" + operand2 + " %s ".formatted(s.toUpperCase()) + operand1 + ")");
         }
      }
      return evalStack.pop();
   }

   private static String blockSelectQuery(
         final String type,
         final JsonConfig jsonConfig,
         final int ruleNumber,
         final List<String> postfix,
         final ProbabilisticRule rule) {

      if (rule.vars().size() == 1) {
         return "query query_%s_block_%02d(".formatted(type, ruleNumber)
                + rule.vars().stream().map("$%s: string"::formatted).collect(Collectors.joining(","))
                + """
                  ) {
                     all(func:type(GoldenRecord)) @filter(match(GoldenRecord.%s,$%s,3)) {
                        uid
                        GoldenRecord.source_id {
                           uid
                        }
                        GoldenRecord.aux_date_created
                        GoldenRecord.aux_auto_update_enabled
                        GoldenRecord.aux_id
                  """.formatted(rule.vars().getFirst(), rule.vars().getFirst())
                + jsonConfig.demographicFields()
                            .stream()
                            .map(demographicField -> "      GoldenRecord.%s".formatted(demographicField.scFieldName()))
                            .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator()
                + """
                     }
                  }
                  """;
      } else {
         final var filters = postfix
               .stream()
               .filter(o -> !(o.equals("and") || o.equals("or")))
               .map(o -> {
                  var matcher = Pattern.compile("^eq\\((?<field>\\w+)\\)$").matcher(o);
                  if (matcher.find()) {
                     final var field = matcher.group("field");
                     return new CompileMetaData(
                           AppUtils.snakeToCamelCase("eq_%s".formatted(field)),
                           "eq",
                           field,
                           null);
                  }
                  matcher = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$").matcher(o);
                  if (matcher.find()) {
                     final var field = matcher.group("field");
                     final var distance = Integer.valueOf(matcher.group("distance"));
                     return new CompileMetaData(
                           AppUtils.snakeToCamelCase("match_%d_%s".formatted(distance, field)),
                           "match",
                           field,
                           distance);
                  }
                  return new CompileMetaData(null, null, null, null);
               })
               .collect(Collectors.toSet())
               .stream().toList();
         final var vars = rule.vars().stream().map("$%s: string"::formatted).collect(Collectors.joining(", "));
         final var aliasFilters = filters
               .stream()
               .map(filter -> {
                  return """
                            var(func:type(GoldenRecord)) @filter(%s(GoldenRecord.%s, $%s%s)) {
                               %s as uid
                            }
                         """.formatted(filter.func, filter.field, filter.field,
                                       filter.param == null
                                             ? ""
                                             : ", %d".formatted(filter.param),
                                       filter.alias);
               })
               .collect(Collectors.joining(System.lineSeparator()));
         return "query query_%s_deterministic_%02d(".formatted(type, ruleNumber)
                + vars
                + ") {"
                + System.lineSeparator()
                + aliasFilters
                + """
                  
                     all(func:type(GoldenRecord)) @filter(%s) {
                        uid
                        GoldenRecord.source_id {
                           uid
                        }
                        GoldenRecord.aux_date_created
                        GoldenRecord.aux_auto_update_enabled
                        GoldenRecord.aux_id
                  """.formatted(postfixToInfix2(filters, postfix))
                + jsonConfig.demographicFields()
                            .stream()
                            .map(demographicField -> "      GoldenRecord.%s".formatted(demographicField.scFieldName()))
                            .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator()
                + """
                     }
                  }
                  """;
      }
   }

   private static String deterministicSelectQuery(
         final String type,
         final JsonConfig jsonConfig,
         final int ruleNumber,
         final List<String> postfix,
         final DeterministicRule rule) {
      if (rule.vars().size() == 1) {
         return "query query_%s_deterministic_%02d(".formatted(type, ruleNumber)
                + rule.vars().stream().map("$%s: string"::formatted).collect(Collectors.joining(","))
                + """
                  ) {
                     all(func:type(GoldenRecord)) @filter(eq(GoldenRecord.%s,$%s)) {
                        uid
                        GoldenRecord.source_id {
                           uid
                        }
                        GoldenRecord.aux_date_created
                        GoldenRecord.aux_auto_update_enabled
                        GoldenRecord.aux_id
                  """.formatted(rule.vars().getFirst(), rule.vars().getFirst())
                + jsonConfig.demographicFields()
                            .stream()
                            .map(demographicField -> "      GoldenRecord.%s".formatted(demographicField.scFieldName()))
                            .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator()
                + """
                     }
                  }
                  """;
      } else {
         return "query query_%s_deterministic_%02d(".formatted(type, ruleNumber)
                + rule.vars().stream().map("$%s: string"::formatted).collect(Collectors.joining(", "))
                + ") {"
                + System.lineSeparator()
                + IntStream
                      .range(0, rule.vars().size())
                      .mapToObj(varIdx -> {
                         final var varName = rule.vars().get(varIdx);
                         return """
                                   var(func:type(GoldenRecord)) @filter(eq(GoldenRecord.%s, $%s)) {
                                      %c as uid
                                   }
                                """.formatted(varName, varName, 'A' + varIdx);
                      })
                      .collect(Collectors.joining(System.lineSeparator()))
                + """
                  
                     all(func:type(GoldenRecord)) @filter(%s) {
                        uid
                        GoldenRecord.source_id {
                           uid
                        }
                        GoldenRecord.aux_date_created
                        GoldenRecord.aux_auto_update_enabled
                        GoldenRecord.aux_id
                  """.formatted(postfixToInfix1(rule.vars(), postfix))
                + jsonConfig.demographicFields()
                            .stream()
                            .map(demographicField -> "      GoldenRecord.%s".formatted(demographicField.scFieldName()))
                            .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator()
                + """
                     }
                  }
                  """;


      }
   }

   /**
    * Generate deterministic programs list.
    *
    * @param jsonConfig the json config
    * @param rules      the rules
    * @return the list
    */
   public static List<DeterministicProgram> generateDeterministicPrograms(
         final String type,
         final JsonConfig jsonConfig,
         final List<DeterministicRule> rules) {
      final List<DeterministicProgram> deterministicPrograms = new ArrayList<>();
      if (rules.isEmpty()) {
         return deterministicPrograms;
      }
      IntStream.range(0, rules.size()).forEach(i -> {
         final var deterministicRule = rules.get(i);
         final var infix = Arrays.asList(deterministicRule.text().split(" "));
         final var rpn = shuntingYard(infix);
         LOGGER.debug("{}", deterministicRule.text());
         LOGGER.debug("{}", infix);
         LOGGER.debug("{}", rpn);
         final List<Operation> canApplyProgram = new ArrayList<>();
         final List<Operation> program = new ArrayList<>();
         var stackDepth = 0;
         final var selectQuery = deterministicSelectQuery(type, jsonConfig, i, rpn, deterministicRule);
         LOGGER.info("{}", selectQuery);
         for (final String s : rpn) {
            if (s.startsWith("eq")) {
               final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(Programs::eq, fieldIndex, null));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("match")) {
               final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var distance = Integer.valueOf(matcher.group("distance"));
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(Programs::match, fieldIndex, distance));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("null")) {
               final var pattern = Pattern.compile("^null\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldIsBlank, fieldIndex, null));
                  program.add(new Operation(Programs::isNull, fieldIndex, null));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("switched")) {
               final var pattern = Pattern.compile("^switched\\((?<field1>\\w+),(?<field2>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field1 = matcher.group("field1");
                  final var field2 = matcher.group("field2");
                  final var field1Index = fieldIndexOf(jsonConfig, field1);
                  final var field2Index = fieldIndexOf(jsonConfig, field2);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, field1Index, null));
                  program.add(new Operation(Programs::switched, field1Index, field2Index));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("and")) {
               canApplyProgram.add(new Operation(Programs::andOperator, null, null));
               program.add(new Operation(Programs::andOperator, null, null));
               stackDepth -= 1;
            } else if (s.startsWith("or")) {
               canApplyProgram.add(new Operation(Programs::orOperator, null, null));
               program.add(new Operation(Programs::orOperator, null, null));
               stackDepth -= 1;
            } else {
               LOGGER.error("NOT HANDLED: [{}]", s);
            }
         }
         if (stackDepth != 1) {
            LOGGER.error("Stack Depth error: {}", stackDepth);
         } else {
            LOGGER.debug("Stack Depth {}", stackDepth);
            deterministicPrograms.add(new DeterministicProgram(selectQuery, program, canApplyProgram));
         }
      });
      return deterministicPrograms;
   }

   public static List<BlockProgram> generateBlockPrograms(
         final String type,
         final JsonConfig jsonConfig,
         final List<ProbabilisticRule> rules) {
      final List<BlockProgram> blockPrograms = new ArrayList<>();
      if (rules.isEmpty()) {
         return blockPrograms;
      }
      IntStream.range(0, rules.size()).forEach(i -> {
         final var rule = rules.get(i);
         final var infix = Arrays.asList(rule.text().split(" "));
         final var rpn = shuntingYard(infix);
         LOGGER.debug("{}", rule.text());
         LOGGER.debug("{}", infix);
         LOGGER.debug("{}", rpn);
         final List<Operation> canApplyProgram = new ArrayList<>();
         final List<Operation> program = new ArrayList<>();
         var stackDepth = 0;
         final var selectQuery = blockSelectQuery(type, jsonConfig, i, rpn, rule);
         LOGGER.info("{}", selectQuery);
         for (final String s : rpn) {
            if (s.startsWith("eq")) {
               final var pattern = Pattern.compile("^eq\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(Programs::eq, fieldIndex, null));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("match")) {
               final var pattern = Pattern.compile("^match\\((?<field>\\w+),(?<distance>\\d+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var distance = Integer.valueOf(matcher.group("distance"));
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, fieldIndex, null));
                  program.add(new Operation(Programs::match, fieldIndex, distance));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("isNull")) {
               final var pattern = Pattern.compile("^isNull\\((?<field>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field = matcher.group("field");
                  final var fieldIndex = fieldIndexOf(jsonConfig, field);
                  canApplyProgram.add(new Operation(Programs::interactionFieldIsBlank, fieldIndex, null));
                  program.add(new Operation(Programs::isNull, fieldIndex, null));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("switched")) {
               final var pattern = Pattern.compile("^switched\\((?<field1>\\w+),(?<field2>\\w+)\\)$");
               final var matcher = pattern.matcher(s);
               if (matcher.find()) {
                  final var field1 = matcher.group("field1");
                  final var field2 = matcher.group("field2");
                  final var field1Index = fieldIndexOf(jsonConfig, field1);
                  final var field2Index = fieldIndexOf(jsonConfig, field2);
                  canApplyProgram.add(new Operation(Programs::interactionFieldNotBlank, field1Index, null));
                  program.add(new Operation(Programs::switched, field1Index, field2Index));
                  stackDepth += 1;
               } else {
                  LOGGER.error("Match error: [{}]", s);
               }
            } else if (s.startsWith("and")) {
               canApplyProgram.add(new Operation(Programs::andOperator, null, null));
               program.add(new Operation(Programs::andOperator, null, null));
               stackDepth -= 1;
            } else if (s.startsWith("or")) {
               canApplyProgram.add(new Operation(Programs::orOperator, null, null));
               program.add(new Operation(Programs::orOperator, null, null));
               stackDepth -= 1;
            } else {
               LOGGER.error("NOT HANDLED: [{}]", s);
            }
         }
         if (stackDepth != 1) {
            LOGGER.error("Stack Depth error: {}", stackDepth);
         } else {
            LOGGER.debug("Stack Depth {}", stackDepth);
            blockPrograms.add(new BlockProgram(selectQuery, program, canApplyProgram));
         }
      });
      return blockPrograms;
   }

   private static int fieldIndexOf(
         final JsonConfig jsonConfig,
         final String field) {
      for (int f = 0; f < jsonConfig.demographicFields().size(); f++) {
         if (jsonConfig.demographicFields().get(f).scFieldName().equals(field)) {
            return f;
         }
      }
      return -1;
   }

   /**
    * Run deterministic programs boolean.
    *
    * @param programs     the programs
    * @param interaction  the interaction
    * @param goldenRecord the golden record
    * @return the boolean
    */
   public static boolean runDeterministicPrograms(
         final List<DeterministicProgram> programs,
         final DemographicData interaction,
         final DemographicData goldenRecord) {
      for (final var program : programs) {
         final Deque<Boolean> evalStack = new ArrayDeque<>();
         for (final var operation : program.program) {
            operation.opcode()
                     .accept(evalStack,
                             new Arguments(interaction.fields,
                                           goldenRecord.fields,
                                           operation.field(),
                                           operation.aux()));
         }
         if (Boolean.TRUE.equals(evalStack.pop())) {
//            LOGGER.debug("Deterministic match: {}={}",
//                         goldenRecord.fields.get(Config.FIELDS_CONFIG.findIndexOfDemographicField("pin")),
//                         interaction.fields.get(Config.FIELDS_CONFIG.findIndexOfDemographicField("pin")));
            return true;
         }
      }
      return false;
   }

   /**
    * Can apply deterministic linking boolean.
    *
    * @param program     the program
    * @param interaction the interaction
    * @return the boolean
    */
   public static boolean canApplyDeterministicLinking(
         final DeterministicProgram program,
         final DemographicData interaction) {
      final Deque<Boolean> evalStack = new ArrayDeque<>();
      for (final var operation : program.canApplyProgram) {
         operation.opcode()
                  .accept(evalStack,
                          new Arguments(interaction.fields, null, operation.field(), operation.aux()));
      }
      return Boolean.TRUE.equals(evalStack.pop());
   }

   /**
    * Can apply linking boolean.
    *
    * @param probabilisticLinkFields the probabilistic link fields
    * @param programs                the programs
    * @param interaction             the interaction
    * @return the boolean
    */
   public static boolean canApplyLinking(
         final List<LinkerConfig.FieldProbabilisticMetaData> probabilisticLinkFields,
         final List<DeterministicProgram> programs,
         final DemographicData interaction) {
      if (!probabilisticLinkFields.isEmpty()) {
         return true;
      }
      for (final var program : programs) {
         if (canApplyDeterministicLinking(program, interaction)) {
            return true;
         }
      }
      return false;
   }

   private record CompileMetaData(
         String alias,
         String func,
         String field,
         Integer param) {
   }

   /**
    * The type Operation.
    */
   public record Operation(
         BiConsumer<Deque<Boolean>, Arguments> opcode,
         Integer field,
         Integer aux) {
   }

   /**
    * The type Arguments.
    */
   public record Arguments(
         List<DemographicData.DemographicField> interaction,
         List<DemographicData.DemographicField> goldenRecord,
         Integer field,
         Integer aux) {
   }

   public record DeterministicProgram(
         String selectQuery,
         List<Operation> program,
         List<Operation> canApplyProgram) {
   }

   public record BlockProgram(
         String selectQuery,
         List<Operation> program,
         List<Operation> canApplyProgram) {
   }

}
