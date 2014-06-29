package io.collap.bryg.compiler.expression;

import java.util.HashMap;
import java.util.Map;

public class Operators {

    private static Map<String, Operator> operators = new HashMap<> ();

    static {
        operators.put ("==", Operator.equality);
        operators.put ("!=", Operator.inequality);
        operators.put (">", Operator.relational_greater_than);
        operators.put (">=", Operator.relational_greater_equal);
        operators.put ("<", Operator.relational_less_than);
        operators.put ("<=", Operator.relational_less_equal);
        operators.put ("+", Operator.addition);
    }

    public static Operator fromString (String operatorString) {
        return operators.get (operatorString);
    }

}
