package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerDeterministic {

  private val classLocation = "../JeMPI_Shared_Source/custom"
  private val custom_className = "CustomLinkerDeterministic"
  private val packageText = "org.jembi.jempi.linker"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)
    writer.println(s"package $packageText;")
    writer.println()
    writer.println(
      """import org.apache.commons.lang3.StringUtils;
        |
        |import org.jembi.jempi.shared.models.CustomPatient;
        |import org.jembi.jempi.shared.models.CustomGoldenRecord;
        |""".stripMargin)
    writer.println(s"class $custom_className {")
    writer.println()
    writer.println(" " * 3 + s"private $custom_className() {}")
    writer.println()
    emitDeterminsticMatch(writer, config.rules)
    writer.println("}")
    writer.flush()
    writer.close()
  }

  def emitDeterminsticMatch(writer: PrintWriter, rules: Rules): Unit = {

    writer.println(
      """   private static boolean isMatch(final String left, final String right) {
        |      return StringUtils.isNotBlank(left) && StringUtils.equals(left, right);
        |   }
        |""".stripMargin)

    def checkNullExpression(expr: Ast.Expression): String = {

      expr match {
        case Ast.Or(x) => "("
          + (for (k <- x.zipWithIndex) yield if (k._2 == 0) checkNullExpression(k._1) else " || " + checkNullExpression(k._1)).mkString
          + ")"
        case Ast.And(x) => "("
          + (for (k <- x.zipWithIndex) yield if (k._2 == 0) checkNullExpression(k._1) else " && " + checkNullExpression(k._1)).mkString
          + ")"
        case Ast.Not(x) =>
          "NOT (" + checkNullExpression(x) + ")"
        case Ast.Match(variable, _) =>
          val field = Utils.snakeCaseToCamelCase(variable.name)
          val left = field + "_l"
          val right = field + "_r"
          s"isMatch($left, $right)"
        case Ast.Eq(variable) =>
          val field = Utils.snakeCaseToCamelCase(variable.name)
          val left = field + "_l"
          val right = field + "_r"
          s"isMatch($left, $right)"
        case _ =>
          "ERROR"
      }
    }

    writer.println(
      """   static boolean deterministicMatch(final CustomGoldenRecord goldenRecord,
        |                                     final CustomPatient patient) {""".stripMargin)
    val map = rules.deterministic
    map.foreach((_, rule) => {
      val expression: Ast.Expression = ParseRule.parse(rule.text)
      val expr_1 = checkNullExpression(expression)
      rule.vars.foreach(v => {
        val field = Utils.snakeCaseToCamelCase(v)
        val left = field + "_l"
        val right = field + "_r"
        writer.println(" " * 6 + s"final var $left = goldenRecord.$field();")
        writer.println(" " * 6 + s"final var $right = patient.$field();")
      })
      writer.println(s"      return $expr_1;")
    })
    writer.println(
      """   }
        |""".stripMargin)

  }

}