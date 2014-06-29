package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;

public class StringBuilderCompileHelper extends ObjectCompileHelper {

    public StringBuilderCompileHelper (BrygMethodVisitor method) {
        super (method, new Type (StringBuilder.class));
    }

    public void compileAppend (Expression expression) {
        compileAppend (expression, true);
    }

    /**
     * StringBuilder -> StringBuilder
     */
    public void compileAppend (Expression expression, boolean compileExpression) {
        Type argumentType = expression.getType ();
        if (!argumentType.getJavaType ().isPrimitive ()) { /* All objects are supplied as either String or Object. */
            if (!argumentType.equals (String.class)) {
                argumentType = new Type (Object.class);
            }
        }else {
            /* Byte and Short primitives need to be appended as integers. */
            if (argumentType.equals (Byte.TYPE) || argumentType.equals (Short.TYPE)) {
                argumentType = new Type (Integer.TYPE);
            }
        }

        if (compileExpression) {
            expression.compile ();
            // -> T
        }

        compileInvokeVirtual ("append", TypeHelper.generateMethodDesc (
                new Type[] { argumentType },
                type
        ));
        // StringBuilder, T -> StringBuilder
    }

    public void compileToString () {
        compileInvokeVirtual ("toString", TypeHelper.generateMethodDesc (null, String.class));
        // StringBuilder -> String
    }

}
