package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

import java.util.Arrays;
import java.util.List;

public class HTMLAttributeCompiler {

    private BrygMethodVisitor method;
    private List<ArgumentExpression> arguments;
    private String[] validAttributes;

    public HTMLAttributeCompiler (BrygMethodVisitor method, List<ArgumentExpression> arguments, String[] validAttributes) {
        this.method = method;
        this.arguments = arguments;
        this.validAttributes = validAttributes;
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
                    if (attribute.getType () != String.class) {
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
