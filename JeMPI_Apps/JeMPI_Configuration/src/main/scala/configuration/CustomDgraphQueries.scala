package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object CustomDgraphQueries {

  private val classLocation =
    "../JeMPI_LibMPI/src/main/java/org/jembi/jempi/libmpi/dgraph"
  private val custom_className = "CustomDgraphQueries"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def generate(config: Config): Any = {

    val classFile: String =
      classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(s"""package $packageText;
         |
         |import io.vavr.Function1;
         |import org.apache.commons.lang3.StringUtils;
         |import org.jembi.jempi.shared.models.DemographicData;
         |import org.jembi.jempi.shared.models.GoldenRecord;
         |
         |import java.util.LinkedList;
         |import java.util.List;
         |import java.util.Map;
         |
         |
         |import static org.jembi.jempi.shared.models.CustomDemographicData.*;
         |
         |import static org.jembi.jempi.libmpi.dgraph.DgraphQueries.runGoldenRecordsQuery;
         |
         |final class $custom_className {
         |
         |   static final List<Function1<DemographicData, List<GoldenRecord>>> DETERMINISTIC_LINK_FUNCTIONS =
         |      List.of(${getDeterministicFunctions(config.rules.link.get)});
         |""".stripMargin)

    if (config.rules.matchNotification.isDefined) {
      writer.println(
        s"""   static final List<Function1<DemographicData, List<GoldenRecord>>> DETERMINISTIC_MATCH_FUNCTIONS =
           |      List.of(${getDeterministicFunctions(
            config.rules.matchNotification.get
          )});
           |""".stripMargin
      )
    } else {
      writer.println(
        s"""   static final List<Function1<DemographicData, List<GoldenRecord>>> DETERMINISTIC_MATCH_FUNCTIONS =
           |      List.of();
           |""".stripMargin
      )
    }

    if (config.rules.link.get.deterministic.isDefined) {
      config.rules.link.get.deterministic.get.foreach((name, rule) =>
        emitRuleTemplate(name, rule)
      )
    }
    if (
      config.rules.link.isDefined && config.rules.link.get.probabilistic.isDefined
    ) {
      config.rules.link.get.probabilistic.get.foreach((name, rule) =>
        emitRuleTemplate(name, rule)
      )
    }

    if (
      config.rules.matchNotification.isDefined && config.rules.matchNotification.get.deterministic.isDefined
    ) {
      config.rules.matchNotification.get.deterministic.get.foreach(
        (name, rule) => emitRuleTemplate(name, rule)
      )
    }
    if (
      config.rules.matchNotification.isDefined && config.rules.matchNotification.get.probabilistic.isDefined
    ) {
      config.rules.matchNotification.get.probabilistic.get.foreach(
        (name, rule) => emitRuleTemplate(name, rule)
      )
    }

    if (
      config.rules.link.isDefined && config.rules.link.get.deterministic.isDefined
    ) {
      config.rules.link.get.deterministic.get.foreach((name, rule) =>
        emitRuleFunction(name, rule)
      )
    }
    if (
      config.rules.link.isDefined && config.rules.link.get.probabilistic.isDefined
    ) {
      config.rules.link.get.probabilistic.get.foreach((name, rule) =>
        emitRuleFunction(name, rule)
      )
    }

    emitMergeCandidates()

    emitFindCandidates(
      "Link",
      "DETERMINISTIC_LINK_FUNCTIONS",
      config.rules.link
    )

    if (
      config.rules.matchNotification.isDefined && config.rules.matchNotification.get.deterministic.isDefined
    ) {
      config.rules.matchNotification.get.deterministic.get.foreach(
        (name, rule) => emitRuleFunction(name, rule)
      )
    }
    if (
      config.rules.matchNotification.isDefined && config.rules.matchNotification.get.probabilistic.isDefined && config.rules.matchNotification.get.probabilistic.isDefined
    ) {
      config.rules.matchNotification.get.probabilistic.get.foreach(
        (name, rule) => emitRuleFunction(name, rule)
      )
    }
    emitFindCandidates(
      "Match",
      "DETERMINISTIC_MATCH_FUNCTIONS",
      config.rules.matchNotification
    )

    writer.println(s"""   private $custom_className() {
         |   }
         |
         |}""".stripMargin)
    writer.flush()
    writer.close()

    def getDeterministicFunctions(allRules: AllRules): String = {
      allRules.deterministic.get
        .map((name, _) =>
          s"""${" " * 14}CustomDgraphQueries::${Utils.snakeCaseToCamelCase(
              name.toLowerCase
            )},""".stripMargin
        )
        .mkString(sys.props("line.separator"))
        .trim
        .dropRight(1)
    }

    def emitFindCandidates(
        funcQualifier: String,
        filterList: String,
        rules: Option[AllRules]
    ): Unit = {
      writer.println(
        s"""   static List<GoldenRecord> find${funcQualifier}Candidates(
           |      final DemographicData interaction) {
           |      var result = DgraphQueries.deterministicFilter($filterList, interaction);
           |      if (!result.isEmpty()) {
           |         return result;
           |      }
           |      result = new LinkedList<>();""".stripMargin
      )
      if (rules.isDefined && rules.get.probabilistic.isDefined) {
        rules.get.probabilistic.get.foreach((name, _) => {
          val filterName = Utils.snakeCaseToCamelCase(name.toLowerCase)
          val vars = "interaction"
          writer.println(
            s"""${" " * 6}mergeCandidates(result, $filterName($vars));""".stripMargin
          )
        })
      }
      writer.println("""      return result;
          |   }
          |""".stripMargin)
    }

    def emitRuleFunction(name: String, rule: Rule): Unit = {

      def expression(expr: Ast.Expression): String = {
        expr match {
          case Ast.Or(x) =>
            "("
              + (for (
                k <- x.filter(v => !v.isInstanceOf[Ast.Null]).zipWithIndex
              )
                yield
                  if (k._2 == 0) expression(k._1)
                  else " && " + expression(k._1)).mkString
              + ")"
          case Ast.And(x) =>
            "("
              + (for (
                k <- x.filter(v => !v.isInstanceOf[Ast.Null]).zipWithIndex
              )
                yield
                  if (k._2 == 0) expression(k._1)
                  else " || " + expression(k._1)).mkString
              + ")"
          case Ast.Not(x) =>
            "NOT (" + expression(x) + ")"
          case Ast.Match(variable, _) =>
            Utils.snakeCaseToCamelCase(variable.name) + "IsBlank"
          case Ast.Eq(variable) =>
            Utils.snakeCaseToCamelCase(variable.name) + "IsBlank"
          case Ast.Null(variable) =>
            "false"
          case _ =>
            "ERROR"
        }
      }

      val vars = for (v <- rule.vars) yield v
      val text = rule.text

      val functionName = Utils.snakeCaseToCamelCase(name.toLowerCase)
      if (vars.length == 1)
        val v = vars(0)
        writer.println(
          s"""   private static List<GoldenRecord> $functionName(final DemographicData demographicData) {
             |      if (StringUtils.isBlank(demographicData.fields.get(FIELD_IDX_${v.toUpperCase}).value())) {
             |         return List.of();
             |      }
             |      final Map<String, String> map = Map.of("$$$v", demographicData.fields.get(FIELD_IDX_${v.toUpperCase}).value());
             |      return runGoldenRecordsQuery($name, map);
             |   }
             |""".stripMargin
        )
      else
        val expr = expression(ParseRule.parse(text))
        writer.println(
          s"   private static List<GoldenRecord> $functionName(final DemographicData demographicData) {"
        )
        vars.foreach(v => {
          val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
          writer.println(
            s"      final var $camelCaseVarName = demographicData.fields.get(FIELD_IDX_${v.toUpperCase}).value();"
          )
        })
        vars.foreach(v => {
          val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
          val isNullVar = camelCaseVarName + s"IsBlank"
          writer.println(
            s"      final var $isNullVar = StringUtils.isBlank($camelCaseVarName);"
          )
        })
        writer.print(s"""      if ($expr) {
             |         return List.of();
             |      }
             |      final var map = Map.of(""".stripMargin)

        vars.zipWithIndex.foreach((v, idx) => {
          val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
          writer.println(s"""${" " * (if (idx == 0) 0 else 29)}"$$$v",
               |${" " * 29}StringUtils.isNotBlank($camelCaseVarName)
               |${" " * 29}      ? $camelCaseVarName
               |${" " * 29}      : DgraphQueries.EMPTY_FIELD_SENTINEL${
                             if (idx + 1 < vars.length) "," else ");"
                           }""".stripMargin)
        })
        writer.println(s"""      return runGoldenRecordsQuery($name, map);
             |   }
             |""".stripMargin)
      end if
    }

    def emitRuleTemplate(name: String, rule: Rule): Unit = {

      case class VarMeta(
          astName: String,
          func: String,
          funcParam: Option[Function1[String, String]],
          funcName: String,
          distance: Option[Integer]
      )

      val vars = for (v <- rule.vars) yield v
      var varsMeta = Map[String, List[VarMeta]]()

      vars.foreach(v => {
        if (!varsMeta.contains(v)) {
          varsMeta += (v -> List[VarMeta]())
        }
      })

      var currentFuncIndex: Int = -1
      val text = rule.text
      val expression: Ast.Expression = ParseRule.parse(text)

      def addMeta(
          v: String,
          astName: String,
          func: String,
          funcParam: Option[Function1[String, String]],
          distance: Option[Integer]
      ): String = {

        if (!varsMeta.contains(v)) {
          varsMeta += (v -> List[VarMeta]())
        }

        val existingMeta = varsMeta(v).find(m => {
          m.astName == astName &&
          m.func == func &&
          m.funcParam.toString == funcParam.toString &&
          m.distance.toString == distance.toString
        })

        if (existingMeta.isDefined) {
          existingMeta.get.funcName
        } else {
          val newMeta =
            VarMeta(astName, func, funcParam, getFuncIndex, distance)
          varsMeta = varsMeta + (v -> (varsMeta(v) :+ newMeta))
          newMeta.funcName
        }
      }

      def getFuncIndex: String = {
        currentFuncIndex = currentFuncIndex + 1
        ("A".head + currentFuncIndex).toChar.toString
      }

      def main_func(expression: Ast.Expression): String = {
        expression match {
          case Ast.Or(x) =>
            "("
              + (for (k <- x.zipWithIndex)
                yield
                  if (k._2 == 0) main_func(k._1)
                  else " OR " + main_func(k._1)).mkString
              + ")"
          case Ast.And(x) =>
            "("
              + (for (k <- x.zipWithIndex)
                yield
                  if (k._2 == 0) main_func(k._1)
                  else " AND " + main_func(k._1)).mkString
              + ")"
          case Ast.Not(x) =>
            "NOT (" + main_func(x) + ")"
          case Ast.Match(variable, distance) =>
            val uidName =
              addMeta(variable.name, "match", "match", None, Option(distance))
            "uid(" + uidName + ")"
          case Ast.Eq(variable) =>
            val uidName = addMeta(variable.name, "eq", "eq", None, None)
            "uid(" + uidName + ")"
          case Ast.Null(variable) =>
            val uidName = addMeta(
              variable.name,
              "null",
              "eq",
              Option(new Function1[String, String] {
                def apply(x: String): String = "\"\""
              }),
              None
            )
            "uid(" + uidName + ")"
          case _ =>
            "ERROR"
        }
      }

      def getFilterParams(v: String, metaInfo: VarMeta): String = {
        if (metaInfo.funcParam.isDefined) {
          return metaInfo.funcParam.get.apply(v)
        }

        s"""$$$v${
            if (metaInfo.distance.isDefined) "," + metaInfo.distance.get
            else ""
          }"""
      }

      def createScalarFunc(): Unit = {
        varsMeta.foreach((v, mL) => {
          val m = mL.head
          val fn = m.func
          writer.println(
            s"""${" " * 12}all(func:type(GoldenRecord)) @filter($fn(GoldenRecord.demographic_field_%02d,${getFilterParams(
                v,
                m
              )})) {
               |${" " * 15}uid
               |${" " * 15}GoldenRecord.source_id {
               |${" " * 15}   uid
               |${" " * 15}}"""
              .format(((fieldName: String) => {
                var i = -1
                config.demographicFields.zipWithIndex.foreach((f, idx) =>
                  if (f.fieldName == fieldName) i = idx
                )
                i
              })(v))
              .stripMargin
          )
          if (config.uniqueGoldenRecordFields.isDefined) {
            config.uniqueGoldenRecordFields.get.foreach(field => {
              writer.println(s"${" " * 15}GoldenRecord.${field.fieldName}")
            })
          }
          config.demographicFields.zipWithIndex
            .foreach((field, idx) => {
              writer.println(
                s"${" " * 15}GoldenRecord.demographic_field_%02d"
                  .format(idx)
              )
            })
          writer.println(s"${" " * 12}}")
        })
      }

      def createFilterFunc(all_func_str: String): Unit = {
        varsMeta.foreach((v, mL) => {
          mL.foreach(m => {
            val fn = m.func
            writer.println(
              s"""${" " * 12}var(func:type(GoldenRecord)) @filter($fn(GoldenRecord.demographic_field_%02d, ${getFilterParams(
                  v,
                  m
                )})) {
                  |${" " * 15}${m.funcName} as uid
                  |${" " * 12}}"""
                .format(((fieldName: String) => {
                  var i = -1
                  config.demographicFields.zipWithIndex.foreach((f, idx) =>
                    if (f.fieldName == fieldName) i = idx
                  )
                  i
                })(v))
                .stripMargin
            )
          })
        })

        writer.println(s"""${" " * 12}all(func:type(GoldenRecord)) @filter${
                           if (all_func_str.startsWith("(")) "" else "("
                         }$all_func_str${
                           if (all_func_str.startsWith("(")) "" else "("
                         } {
             |${" " * 15}uid
             |${" " * 15}GoldenRecord.source_id {
             |${" " * 18}uid
             |${" " * 15}}""".stripMargin)
        if (config.uniqueGoldenRecordFields.isDefined) {
          config.uniqueGoldenRecordFields.get.foreach(field => {
            writer.println(s"${" " * 15}GoldenRecord.${field.fieldName}")
          })
        }
        config.demographicFields.zipWithIndex.foreach((field, idx) => {
          writer.println(
            s"${" " * 15}GoldenRecord.demographic_field_%02d"
              .format(idx)
          )
        })
        writer.println(s"${" " * 12}}")
      }

      var all_func_str = main_func(expression)

      writer.print(
        s"""${" " * 3}private static final String ${name.toUpperCase} =
           |${" " * 9}\"\"\"
           |${" " * 9}query ${name.toLowerCase}(""".stripMargin
      )

      vars.zipWithIndex.foreach((v, i) => {
        if (i > 0)
          writer.print(", ")
        end if
        writer.print(s"$$$v: string")
      })
      writer.println(") {")

      if (varsMeta.size == 1 && varsMeta.values.headOption.get.size < 2)
        createScalarFunc()
      else
        createFilterFunc(all_func_str)
      end if

      writer.println(s"""${" " * 9}}
                        |${" " * 9}\"\"\";
                        |""".stripMargin)
    }

    def emitMergeCandidates(): Unit = {
      writer.println(s"""   private static void mergeCandidates(
           |         final List<GoldenRecord> goldenRecords,
           |         final List<GoldenRecord> block) {
           |      if (!block.isEmpty()) {
           |         block.forEach(candidate -> {
           |            var found = false;
           |            for (GoldenRecord goldenRecord : goldenRecords) {
           |               if (candidate.goldenId().equals(goldenRecord.goldenId())) {
           |                  found = true;
           |                  break;
           |               }
           |            }
           |            if (!found) {
           |               goldenRecords.add(candidate);
           |            }
           |         });
           |      }
           |   }
           |""".stripMargin)
    }

  }

}
