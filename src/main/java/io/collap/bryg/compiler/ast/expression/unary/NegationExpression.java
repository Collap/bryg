package io.collap.bryg.compiler.ast.expression.unary;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;

public class NegationExpression extends Expression {

    private Expression child;

    public NegationExpression (Context context, Expression child, int line) {
        super (context);
        this.child = child;
        setLine (line);

        if (!child.getType ().isNumeric ()) {
            // TODO: Convert byte and short to int. (Fix in 0.3 with Improved Coercion)
            throw new BrygJitException ("Can only negate numeric primitive types!", line);
        }
        setType (child.getType ());
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
