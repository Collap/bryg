package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.BoxingExpression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.Type;

public class BoxingUtil {

    /**
     * @return null if the expression could not be unboxed.
     */
    public static UnboxingExpression createUnboxingExpression (Context context, Expression child) {
        Type primitiveType = child.getType ().getPrimitiveType ();

        if (primitiveType == null) {
            return null;
        }

        return new UnboxingExpression (context, child, primitiveType);
    }

    /**
     * @return null if the expression could not be boxed.
     */
    public static BoxingExpression createBoxingExpression (Context context, Expression child) {
        Type wrapperType = child.getType ().getWrapperType ();

        if (wrapperType == null) {
            return null;
        }

        return new BoxingExpression (context, child, wrapperType);
    }

}