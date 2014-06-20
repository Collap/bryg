package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

import java.util.Arrays;
import java.util.List;

public class HTMLArgumentCompiler {

    private BrygMethodVisitor method;
    private List<ArgumentExpression> arguments;
    private String[] acceptedArguments;

    public HTMLArgumentCompiler (BrygMethodVisitor method, List<ArgumentExpression> arguments, String[] acceptedArguments) {
        this.method = method;
        this.arguments = arguments;
        this.acceptedArguments = acceptedArguments;
    }

    public void compile () {
        for (ArgumentExpression argument : arguments) {
            String name = argument.getName ();

            if (Arrays.binarySearch (acceptedArguments, name) < 0) {
                System.out.println ("Warning: The argument " + name + " is not in the HTML5 standard!");
            }

            method.writeConstantString (" " + name + "=\"");

            Object constantValue = argument.getConstantValue ();
            if (constantValue != null) {
                method.writeConstantString (constantValue.toString ());
            }else {
                method.loadWriter ();
                // -> Writer

                argument.compile ();
                // -> value

                // TODO: Accept all values and cast if necessary.
                if (argument.getType () != String.class) {
                    throw new RuntimeException ("Currently only String values are accepted for HTML arguments!");
                }

                method.writeString ();
                // Writer, value ->
            }

            method.writeConstantString ("\"");
        }
    }

}
