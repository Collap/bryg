package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.Type;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.util.TypeHelper;

public class StringBuilderCompileHelper extends ObjectCompileHelper {

    private static final ClassType TYPE = new ClassType (StringBuilder.class);

    public StringBuilderCompileHelper (RenderVisitor visitor) {
        super (visitor, TYPE);
    }

    public void compileAppend (Expression expression) {
        compileAppend (expression, true);
    }

    /**
     * StringBuilder -> StringBuilder
     */
    public void compileAppend (Expression expression, boolean compileExpression) {
        Type argumentType = expression.getType ();
        if (argumentType instanceof ClassType) { /* All objects are supplied as either String or Object. */
            if (!argumentType.equals (ClassType.STRING)) {
                argumentType = ClassType.OBJECT;
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
        compileInvokeVirtual ("toString", TypeHelper.generateMethodDesc (null, ClassType.STRING));
        // StringBuilder -> String
    }

}
