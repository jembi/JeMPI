package configuration

import scala.util.parsing.combinator.JavaTokenParsers

object ParseRule extends JavaTokenParsers {

  import Ast.*

  def parse(str: String): Ast.Expression =
    parseAll(expression, str) match {
      case Success(result, _) => result
      case failedOrIncomplete =>
        throw new RuntimeException(failedOrIncomplete.toString)
    }

  private def expression: Parser[Expression] =
    combinationExpression | leftExpression

  private def combinationExpression: Parser[Expression] =
    comment.? ~> or | and <~ comment.?

  /** Expressions that can be used as left part of recursive expression
    *
    * @return
    */
  private def leftExpression: Parser[Expression] =
    comment.? ~> not | brackets | matchField | eqField <~ comment.?

  private def brackets: Parser[Expression] =
    "(" ~> expression <~ ")"

  private def eqField: Parser[Eq] =
    "eq" ~ "(" ~>! variable <~! ")" ^^ (parameter => Eq.apply(parameter))

  private def matchField: Parser[Match] =
    "match" ~ "(" ~>! matchParameters <~! ")" ^^ (parameters =>
      Match.apply(parameters._1, parameters._2)
    )

  private def matchParameters: Parser[(Variable, Int)] =
    variable ~! matchDistance ^^ (parameters => (parameters._1, parameters._2))

  private def matchDistance: Parser[Int] =
    "," ~>! wholeNumber ^^ (distance => math.max(1, distance.toInt))

  private def variable: Parser[Variable] =
    ident ^^ (v => Variable(v.toLowerCase))

  private def not: Parser[Not] =
    "not" ~> brackets ^^ Not.apply

  private def and: Parser[And] =
    leftExpression ~ ("and" ~> leftExpression).+ ^^ {
      case (left: Expression) ~ (right: Seq[Expression]) => And(left +: right)
    }

  private def or: Parser[Or] =
    (and | leftExpression) ~ ("or" ~> (and | leftExpression)).+ ^^ {
      case left ~ right => Or(left +: right)
    }

  private def comment: Parser[String] =
    """/\*([^*]|\*[^/])*\*/""".r ^^^ ""

}
