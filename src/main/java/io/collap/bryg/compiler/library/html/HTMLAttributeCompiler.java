package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HTMLAttributeCompiler {

    private class ArgumentComparator implements Comparator<ArgumentExpression> {

        @Override
        public int compare (ArgumentExpression o1, ArgumentExpression o2) {
            int result = 0;
            if (o1.getConstantValue () != null) result -= 1;
            if (o2.getConstantValue () != null) result += 1;
            return result;
        }

    }

    private BrygMethodVisitor method;
    private List<ArgumentExpression> arguments;
    private String[] validAttributes;

    public HTMLAttributeCompiler (BrygMethodVisitor method, List<ArgumentExpression> arguments, String[] validAttributes) {
        this.method = method;
        this.arguments = arguments;
        this.validAttributes = validAttributes;

        /* Sort arguments by constant and non-constant to minimize write calls. */
        Collections.sort (arguments, new ArgumentComparator ());
    }

    public void compile () {
        for (ArgumentExpression attribute : arguments) {
            String name = attribute.getName ();

            boolean valid = name.startsWith ("data-")
                    | Arrays.binarySearch (validAttributes, name) >= 0
                    | Arrays.binarySearch (Attributes.validGlobalAttributes, name) >= 0;

            if (!valid) {
                System.out.println ("Warning: The attribute " + name + " is not a valid HTML5 attribute!");
            }

            Object constantValue = attribute.getConstantValue ();
            boolean isEmpty = constantValue != null && constantValue instanceof String
                                && ((String) constantValue).isEmpty ();

            method.writeConstantString (" " + name);

            if (!isEmpty) {
                method.writeConstantString ("=\"");
                if (constantValue != null) {
                    method.writeConstantString (constantValue.toString ());
                } else {
                    method.loadWriter ();
                    // -> Writer

                    attribute.compile ();
                    // -> value

                    // TODO: Accept all values and cast if necessary.
                    if (!attribute.getType ().equals (String.class)) {
                        throw new RuntimeException ("Currently only String values are accepted for HTML arguments!");
                    }

                    method.writeString ();
                    // Writer, value ->
                }

                method.writeConstantString ("\"");
            }
        }
    }

}
