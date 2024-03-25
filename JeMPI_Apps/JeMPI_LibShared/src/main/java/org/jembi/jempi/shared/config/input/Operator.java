package org.jembi.jempi.shared.config.input;

public enum Operator implements Comparable<Operator> {
   OR("or", Associativity.LEFT, 0),
   AND("and", Associativity.LEFT, 5),
   NOT("not", Associativity.RIGHT, 10);

   final Associativity associativity;
   final int precedence;
   final String symbol;

   Operator(
         final String symbol,
         final Associativity associativity,
         final int precedence) {
      this.symbol = symbol;
      this.associativity = associativity;
      this.precedence = precedence;
   }

   public int comparePrecedence(final Operator operator) {
      return this.precedence - operator.precedence;
   }

}
