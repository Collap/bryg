package io.collap.bryg.compiler.library.html;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HTMLAttributeCompiler {

    private class ArgumentComparator implements Comparator<ArgumentExpression> {

        @Override
        public int compare (ArgumentExpression o1, ArgumentExpression o2) {
            int result = 0;
            if (o1.isConstant () && o1.getPredicate () == null) result -= 1;
            if (o2.isConstant () && o2.getPredicate () == null) result += 1;
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
                    || Arrays.binarySearch (validAttributes, name) >= 0
                    || Arrays.binarySearch (Attributes.validGlobalAttributes, name) >= 0;

            if (!valid) {
                // TODO: Add notice for which tag said attribute is not defined. (Fix with Improved Error Handling)
                System.out.println ("Warning: The attribute " + name + " is not a valid HTML5 attribute! Line: " + attribute.getLine ());
            }

            Label nextFalseLabel = attribute.compilePredicate ();

            Object constantValue = attribute.getConstantValue ();
            boolean isEmpty = attribute.isConstant () &&
                                (constantValue instanceof String
                                    && ((String) constantValue).isEmpty ()
                                || constantValue == null); /* Ignore a 'null' constant. */

            method.writeConstantString (" " + name);

            if (!isEmpty) {
                method.writeConstantString ("=\"");
                if (constantValue != null) {
                    method.writeConstantString (constantValue.toString ());
                } else {
                    method.loadWriter ();
                    // -> Writer

                    // TODO: Accept all value types and cast if necessary. (Fix in 0.3 with Improved Coercion)
                    if (attribute.getType ().similarTo (String.class)) {
                        attribute.compile ();
                        // -> value
                    }else {
                        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (method);
                        stringBuilder.compileNew ();
                        stringBuilder.compileAppend (attribute); // Note: The expression is compiled here!
                        stringBuilder.compileToString ();
                        // -> String
                    }

                    method.writeString ();
                    // Writer, value ->
                }

                method.writeConstantString ("\"");
            }

            if (nextFalseLabel != null) {
                method.visitLabel (nextFalseLabel);
            }
        }
    }

}
