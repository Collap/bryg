package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

public class StringBuilderCompileHelper extends ObjectCompileHelper {

    public StringBuilderCompileHelper (BrygMethodVisitor mv) {
        super (mv, Types.fromClass (StringBuilder.class));
    }

    /**
     * Compiles the expression by default!
     */
    public void compileAppend (Expression expression) {
        compileAppend (expression, true);
    }

    /**
     * StringBuilder -> StringBuilder
     */
    public void compileAppend (Expression expression, boolean compileExpression) {
        Type argumentType = expression.getType ();
        if (!argumentType.isPrimitive ()) { /* All objects are supplied as either String or Object. */
            if (!argumentType.similarTo (String.class)) {
                argumentType = Types.fromClass (Object.class);
            }
        }else {
            /* Byte and Short primitives need to be appended as integers. */
            if (argumentType.similarTo (Byte.TYPE) || argumentType.similarTo (Short.TYPE)) {
                argumentType = Types.fromClass (Integer.TYPE);
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
