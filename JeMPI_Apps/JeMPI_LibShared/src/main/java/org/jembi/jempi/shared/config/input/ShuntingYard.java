package org.jembi.jempi.shared.config.input;

import java.util.*;

import static org.jembi.jempi.shared.config.input.Associativity.LEFT;
import static org.jembi.jempi.shared.config.input.Associativity.RIGHT;


public final class ShuntingYard {

   static final Map<String, Operator> OPS = new HashMap<>();

   static {
      // We build a map with all the existing Operators by iterating over the existing Enum
      // and filling up the map with:
      // <K,V> = <Character, Operator(Character, Associativity, Precedence)>
      for (Operator operator : Operator.values()) {
         OPS.put(operator.symbol, operator);
      }
   }

   private ShuntingYard() {
   }

   public static List<String> shuntingYard(final List<String> tokens) {

      final List<String> output = new LinkedList<>();
      final Deque<String> stack = new ArrayDeque<>();

      // For all the input tokens [S1] read the next token [S2]
      for (String token : tokens) {
         if (OPS.containsKey(token)) {
            // Token is an operator [S3]
            while (!stack.isEmpty() && OPS.containsKey(stack.peek())) {
               // While there is an operator (y) at the top of the operators stack and
               // either (x) is left-associative and its precedence is less or equal to
               // that of (y), or (x) is right-associative and its precedence
               // is less than (y)
               //
               // [S4]:
               Operator cOp = OPS.get(token); // Current operator
               Operator lOp = OPS.get(stack.peek()); // Top operator from the stack
               if ((cOp.associativity == LEFT && cOp.comparePrecedence(lOp) <= 0)
                   || (cOp.associativity == RIGHT && cOp.comparePrecedence(lOp) < 0)) {
                  // Pop (y) from the stack S[5]
                  // Add (y) output buffer S[6]
                  output.add(stack.pop());
                  continue;
               }
               break;
            }
            // Push the new operator on the stack S[7]
            stack.push(token);
         } else if ("(".equals(token)) {
            // Else If token is left parenthesis, then push it on the stack S[8]
            stack.push(token);
         } else if (")".equals(token)) {
            // Else If the token is right parenthesis S[9]
            while (!stack.isEmpty() && !stack.peek().equals("(")) {
               // Until the top token (from the stack) is left parenthesis, pop from
               // the stack to the output buffer
               // S[10]
               output.add(stack.pop());
            }
            // Also pop the left parenthesis but don't include it in the output
            // buffer S[11]
            stack.pop();
         } else {
            // Else add token to output buffer S[12]
            output.add(token);
         }
      }

      while (!stack.isEmpty()) {
         // While there are still operator tokens in the stack, pop them to output S[13]
         output.add(stack.pop());
      }

      return output;
   }

}
