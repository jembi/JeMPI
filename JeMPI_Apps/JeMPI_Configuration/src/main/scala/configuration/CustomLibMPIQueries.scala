package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLibMPIQueries {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val custom_className = "CustomLibMPIQueries"
  private val packageText = "org.jembi.jempi.libmpi.dgraph"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(
      s"""package $packageText;
         |
         |import org.apache.commons.lang3.StringUtils;
         |
         |import java.util.LinkedList;
         |import java.util.List;
         |import java.util.Map;
         |
         |import org.jembi.jempi.shared.models.CustomEntity;
         |
         |import static org.jembi.jempi.libmpi.dgraph.Queries.runGoldenRecordsQuery;
         |
         |class $custom_className {
         |
         |   private $custom_className() {}""".stripMargin)
    config.rules.deterministic.foreach((name, rule) => emitRuleTemplate(config.fields, writer, name, rule))
    if (config.rules.probabilistic != null)
      config.rules.probabilistic.foreach((name, rule) => emitRuleTemplate(config.fields, writer, name, rule))
    writer.println()
    config.rules.deterministic.foreach((name, rule) => emitRuleFunction(writer, name, rule))
    if (config.rules.probabilistic != null)
      config.rules.probabilistic.foreach((name, rule) => emitRuleFunction(writer, name, rule))
    emitGetCandidates(writer, config.rules)
    writer.println("}")
    writer.flush()
    writer.close()
  }

  private def emitGetCandidates(writer: PrintWriter, rules: Rules): Unit = {
    writer.println(
      """   private static void updateCandidates(final List<CustomLibMPIGoldenRecord> goldenRecords,
        |                                        final LibMPIGoldenRecordList block) {
        |      final var candidates = block.all();
        |      if (!candidates.isEmpty()) {
        |         candidates.forEach(candidate -> {
        |            var found = false;
        |            for (CustomLibMPIGoldenRecord goldenRecord : goldenRecords) {
        |               if (candidate.uid().equals(goldenRecord.uid())) {
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
        |
        |   static List<CustomLibMPIGoldenRecord> getCandidates(final CustomEntity dgraphEntity,
        |                                                       final boolean applyDeterministicFilter) {
        |
        |      if (applyDeterministicFilter) {
        |         final var result = Queries.deterministicFilter(dgraphEntity);
        |         if (!result.isEmpty()) {
        |            return result;
        |         }
        |      }
        |      var result = new LinkedList<CustomLibMPIGoldenRecord>();""".stripMargin)
    if (rules.probabilistic != null) {
      rules.probabilistic.foreach((name, rule) => {
        val filterName = Utils.snakeCaseToCamelCase(name.toLowerCase)
        val vars = "dgraphEntity"
        writer.println(s"""${" " * 6}updateCandidates(result, $filterName($vars));""".stripMargin)
      })
    }
    writer.println(
      """      return result;
        |   }
        |""".stripMargin)
  }


  private def emitRuleFunction(writer: PrintWriter, name: String, rule: Rule): Unit = {

    def expression(expr: Ast.Expression): String = {
      expr match {
        case Ast.Or(x) =>
          "("
            + (for (k <- x.zipWithIndex) yield if (k._2 == 0) expression(k._1) else " && " + expression(k._1)).mkString
            + ")"
        case Ast.And(x) =>
          "("
            + (for (k <- x.zipWithIndex) yield if (k._2 == 0) expression(k._1) else " || " + expression(k._1)).mkString
            + ")"
        case Ast.Not(x) =>
          "NOT (" + expression(x) + ")"
        case Ast.Match(variable, _) =>
          Utils.snakeCaseToCamelCase(variable.name) + "IsBlank"
        case Ast.Eq(variable) =>
          Utils.snakeCaseToCamelCase(variable.name) + "IsBlank"
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
        s"""   static LibMPIGoldenRecordList $functionName(final CustomEntity customEntity) {
           |      if (StringUtils.isBlank(customEntity.${Utils.snakeCaseToCamelCase(v)}())) {
           |         return new LibMPIGoldenRecordList(List.of());
           |      }
           |      final Map<String, String> map = Map.of("$$$v", customEntity.${Utils.snakeCaseToCamelCase(v)}());
           |      return runGoldenRecordsQuery($name, map);
           |   }
           |""".stripMargin)
    else
      val expr = expression(ParseRule.parse(text))
      writer.println(s"   static LibMPIGoldenRecordList $functionName(final CustomEntity customEntity) {")
      vars.foreach(v => {
        val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
        writer.println(s"      final var $camelCaseVarName = customEntity.$camelCaseVarName();")
      })
      vars.foreach(v => {
        val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
        val isNullVar = camelCaseVarName + s"IsBlank"
        writer.println(s"      final var $isNullVar = StringUtils.isBlank($camelCaseVarName);")
      })
      writer.println(
        s"""      if ($expr) {
           |         return new LibMPIGoldenRecordList(List.of());
           |      }
           |      final var map = Map.of(""".stripMargin)

      vars.zipWithIndex.foreach((v, idx) => {
        val camelCaseVarName = Utils.snakeCaseToCamelCase(v)
        writer.println(
          s"""         "$$$v",
             |         StringUtils.isNotBlank($camelCaseVarName)
             |            ? $camelCaseVarName
             |            : Queries.EMPTY_FIELD_SENTINEL${if (idx + 1 < vars.length) "," else ");"}""".stripMargin)
      })
      writer.println(
        s"""      return runGoldenRecordsQuery($name, map);
           |   }
           |""".stripMargin)
    end if
  }

  private def emitRuleTemplate(fields: Array[Field], writer: PrintWriter, name: String, rule: Rule): Unit = {

    val vars = for (v <- rule.vars) yield v
    val text = rule.text
    val expression: Ast.Expression = ParseRule.parse(text)
    val varsMap = vars.zipWithIndex.toMap[String, Int].map((k, i) => (k, ("A".head + i).toChar.toString))
    var meta = Map[String, (String, Option[Integer])]()

    def main_func(expression: Ast.Expression): String = {
      expression match {
        case Ast.Or(x) => "("
          + (for (k <- x.zipWithIndex) yield if (k._2 == 0) main_func(k._1) else " OR " + main_func(k._1)).mkString
          + ")"
        case Ast.And(x) => "("
          + (for (k <- x.zipWithIndex) yield if (k._2 == 0) main_func(k._1) else " AND " + main_func(k._1)).mkString
          + ")"
        case Ast.Not(x) =>
          "NOT (" + main_func(x) + ")"
        case Ast.Match(variable, distance) =>
          meta += (variable.name -> ("match", Option(distance)))
          "uid(" + variable.name + ")"
        case Ast.Eq(variable) =>
          meta += (variable.name -> ("eq", None))
          "uid(" + variable.name + ")"
        case _ =>
          "ERROR"
      }
    }

    def createScalerFunc(fields: Array[Field]): Unit = {
      vars.foreach(v => {
        val fn = meta(v)._1
        writer.println(
          s"""${" " * 9}all(func: $fn(GoldenRecord.$v, $$$v${
            if (meta(v)._2.isDefined) ", " + meta(v)._2.get else
              ""
          })) {
             |${" " * 12}uid
             |${" " * 12}GoldenRecord.source_id {
             |${" " * 15}uid
             |${" " * 12}}""".stripMargin)
        fields.foreach(field => {
          writer.println(s"${" " * 12}GoldenRecord.${field.fieldName}")
        })
        writer.println(s"${" " * 9}}")
      })
    }

    def createFilterFunc(fields: Array[Field], all_func_str: String): Unit = {
      vars.foreach(v => {
        val fn = meta(v)._1
        writer.println(
          s"""${" " * 9}var(func: $fn(GoldenRecord.$v, $$$v${
            if (meta(v)._2.isDefined) ", " + meta(v)._2.get else ""
          })) {
             |${" " * 12}${varsMap(v)} as uid
             |${" " * 9}}""".stripMargin)
      })
      writer.println(
        s"""${" " * 9}all(func: uid(${(for (field <- varsMap) yield field._2).mkString(",")})) @filter ${
          if (all_func_str.startsWith("(")) "" else "("
        }$all_func_str${if (all_func_str.startsWith("(")) "" else "("} {
           |${" " * 12}uid
           |${" " * 12}GoldenRecord.source_id {
           |${" " * 15}uid
           |${" " * 12}}""".stripMargin)
      fields.foreach(field => {
        writer.println(s"${" " * 12}GoldenRecord.${field.fieldName}")
      })
      writer.println(s"${" " * 9}}")
    }

    var all_func_str = main_func(expression)
    varsMap.foreach((k, v) => {
      all_func_str = all_func_str.replace("uid(" + k + ")", "uid(" + v + ")")
    })

    writer.print(
      s"""${" " * 3}static final String ${name.toUpperCase} =
         |${" " * 6}\"\"\"
         |${" " * 6}query ${name.toLowerCase}(""".stripMargin)

    vars.zipWithIndex.foreach((v, i) => {
      if (i > 0)
        writer.print(", ")
      end if
      writer.print(s"$$$v: string")
    })
    writer.println(") {")

    if (vars.length == 1)
      createScalerFunc(fields)
    else
      createFilterFunc(fields, all_func_str)
    end if
    writer.println(
      s"""${" " * 6}}
         |${" " * 6}\"\"\";
      """.stripMargin)
  }

}
