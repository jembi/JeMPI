package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}

object CustomLinkerDeterministic {

  private val classLocation =
    "../JeMPI_Linker/src/main/java/org/jembi/jempi/linker/backend"
  private val custom_className = "CustomLinkerDeterministic"
  private val packageText = "org.jembi.jempi.linker.backend"

  def generate(config: Config): Any = {
    val classFile: String =
      classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    def emitCanApplyLinking(rules: Map[String, Rule]): Unit = {
      writer.print(
        s"""   static boolean canApplyLinking(
           |         final CustomDemographicData interaction) {
           |      return CustomLinkerProbabilistic.PROBABILISTIC_DO_LINKING""".stripMargin
      )
      rules.zipWithIndex.foreach((rule, rule_idx) => {
        writer.print(s"""
             |             || """.stripMargin)
        rule._2.vars.zipWithIndex.foreach((field, var_idx) =>
          writer.print(
            s"StringUtils.isNotBlank(interaction.fields.get(${field.toUpperCase}).value())${
                if (var_idx + 1 < rule._2.vars.length)
                  s"${sys.props("line.separator")}${" " * 13}&& "
                else ""
              }"
          )
        )
      })
      writer.println(s""";
           |   }
           |""".stripMargin)
    }

    def emitDeterministicMatch(
        funcName: String,
        map: Map[String, Rule]
    ): Unit = {

      def checkNullExpression(expr: Ast.Expression): String = {
        expr match {
          case Ast.Or(x) =>
            "("
              + (for (k <- x.zipWithIndex)
                yield
                  if (k._2 == 0) checkNullExpression(k._1)
                  else " || " + checkNullExpression(k._1)).mkString
              + ")"
          case Ast.And(x) =>
            "("
              + (for (k <- x.zipWithIndex)
                yield
                  if (k._2 == 0) checkNullExpression(k._1)
                  else " && " + checkNullExpression(k._1)).mkString
              + ")"
          case Ast.Not(x) =>
            "NOT (" + checkNullExpression(x) + ")"
          case Ast.Match(variable, _) =>
            val field = Utils.snakeCaseToCamelCase(variable.name)
            val left = field + "L"
            val right = field + "R"
            s"isMatch($left, $right)"
          case Ast.Eq(variable) =>
            val field = Utils.snakeCaseToCamelCase(variable.name)
            val left = field + "L"
            val right = field + "R"
            s"isMatch($left, $right)"
          case Ast.Null(variable) =>
            val field = Utils.snakeCaseToCamelCase(variable.name)
            val right = field + "R"
            s"StringUtils.isBlank($right)"
          case _ =>
            "ERROR"
        }
      }

      writer.println(s"""   static boolean $funcName(
           |         final CustomDemographicData goldenRecord,
           |         final CustomDemographicData interaction) {""".stripMargin)

      if (map.isEmpty) {
        writer.println(s"""      return false;
             |   }
             |""".stripMargin)
      } else {
        val z = map.zipWithIndex
        var definedProperties: List[String] = List()
        z.foreach((map, index) => {
          val expression: Ast.Expression = ParseRule.parse(map._2.text)
          val expr_1 = checkNullExpression(expression)
          map._2.vars.foreach(v => {
            val field = Utils.snakeCaseToCamelCase(v)
            if (!definedProperties.contains(field)) {
              val left = field + "L"
              val right = field + "R"
              writer.println(
                " " * 6 + s"final var $left = goldenRecord.fields.get(${v.toUpperCase}).value();"
              )
              writer.println(
                " " * 6 + s"final var $right = interaction.fields.get(${v.toUpperCase}).value();"
              )
              definedProperties = definedProperties :+ field
            }
          })
          if (index < z.size - 1) {
            writer.println(s"""      if ($expr_1) {
                 |         return true;
                 |      }""".stripMargin)
          } else {
            writer.println(s"""      return $expr_1;""".stripMargin)
          }
        })
        writer.println("""   }
            |""".stripMargin)

      }
    }

    writer.println(
      s"""package $packageText;
         |
         |import org.apache.commons.lang3.StringUtils;
         |
         |import org.jembi.jempi.shared.models.CustomDemographicData;
         |
         |import static org.jembi.jempi.shared.models.CustomDemographicData.*;
         |
         |final class $custom_className {
         |
         |   static final boolean DETERMINISTIC_DO_LINKING = ${
          if (config.rules.link.get.deterministic.nonEmpty) "true" else "false"
        };
         |   static final boolean DETERMINISTIC_DO_VALIDATING = ${
          if (config.rules.validate.nonEmpty) "true" else "false"
        };
         |   static final boolean DETERMINISTIC_DO_MATCHING = ${
          if (config.rules.matchNotification.nonEmpty) "true" else "false"
        };
         |
         |   private $custom_className() {
         |   }
         |
         |   private static boolean isMatch(
         |         final String left,
         |         final String right) {
         |      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
         |   }
         |""".stripMargin
    )
    emitCanApplyLinking(config.rules.link.get.deterministic.get)
    emitDeterministicMatch(
      "linkDeterministicMatch",
      config.rules.link.get.deterministic.get
    )
    emitDeterministicMatch(
      "validateDeterministicMatch",
      if (config.rules.validate.isDefined)
        config.rules.validate.get.deterministic
      else Map.empty[String, Rule]
    )
    emitDeterministicMatch(
      "matchNotificationDeterministicMatch",
      if (config.rules.matchNotification.isDefined)
        config.rules.matchNotification.get.deterministic.get
      else Map.empty[String, Rule]
    )
    writer.println("}")
    writer.flush()
    writer.close()
  }

}
