package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.TypeHelper;

public class StringBuilderCompileHelper extends ObjectCompileHelper {

    public StringBuilderCompileHelper (StandardVisitor visitor) {
        super (visitor, StringBuilder.class);
    }

    public void compileAppend (Expression expression) {
        compileAppend (expression, true);
    }

    /**
     * StringBuilder -> StringBuilder
     */
    public void compileAppend (Expression expression, boolean compileExpression) {
        Class<?> argumentType = expression.getType ();
        if (!argumentType.isPrimitive ()) { /* All objects are supplied as either String or Object. */
            if (!argumentType.equals (String.class)) {
                argumentType = Object.class;
            }
        }

        if (compileExpression) {
            expression.compile ();
            // -> T
        }

        compileInvokeVirtual ("append", TypeHelper.generateMethodDesc (
                new Class<?>[] { argumentType },
                type
        ));
        // StringBuilder, T -> StringBuilder
    }

    public void compileToString () {
        compileInvokeVirtual ("toString", TypeHelper.generateMethodDesc (null, String.class));
        // StringBuilder -> String
    }

}
