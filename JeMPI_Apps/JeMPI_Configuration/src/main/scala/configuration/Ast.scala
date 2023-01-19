package configuration

object Ast {

  sealed trait Expression

  case class And(expression: Seq[Expression]) extends Expression

  case class Or(expression: Seq[Expression]) extends Expression

  case class Not(expression: Expression) extends Expression

  case class Variable(name: String) extends Expression

  case class Match(name: Variable, distance: Integer) extends Expression

  case class Eq(name: Variable) extends Expression

}
