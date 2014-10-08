package io.collap.bryg.compiler.ast.expression.unary;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.exception.BrygJitException;

public class NegationExpression extends Expression {

    private Expression child;

    public NegationExpression (Context context, Expression child, int line) {
        super (context);
        this.child = child;
        setLine (line);

        if (!child.getType ().isNumeric ()) {
            Type unboxedType = BoxingUtil.unboxType (child.getType ());
            if (unboxedType == null) {
                throw new BrygJitException ("Can only negate numeric primitive types!", line);
            }
            this.child = new UnboxingExpression (context, child, unboxedType);
            setType (unboxedType);
        }else {
            setType (child.getType ());
        }

        /* "Convert" to integer if byte or short, since there are no neg operators for byte or short. */
        if (type.similarTo (Byte.TYPE) || type.similarTo (Short.TYPE)) {
            setType (new Type (Integer.TYPE));
        }
    }

    @Override
    public void compile () {
        child.compile ();
        // -> T

        /* Negate the value. */
        int op = type.getAsmType ().getOpcode (Opcodes.INEG);
        context.getMethodVisitor ().visitInsn (op);
        // T -> T
    }

}
