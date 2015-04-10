package io.collap.bryg.internal.compiler.ast.expression.unary;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.BrygJitException;

public class NegationExpression extends Expression {

    private Expression child;

    public NegationExpression (CompilationContext compilationContext, Expression child, int line) {
        super (compilationContext);
        this.child = child;
        setLine (line);

        if (!child.getType ().isNumeric ()) {
            Type primitiveType = child.getType ().getPrimitiveType ();
            if (primitiveType == null) {
                throw new BrygJitException ("Can only negate numeric primitive types!", line);
            }
            this.child = new UnboxingExpression (compilationContext, child, primitiveType);
            setType (primitiveType);
        }else {
            setType (child.getType ());
        }

        /* "Convert" to integer if byte or short, since there are no neg operators for byte or short. */
        if (type.similarTo (Byte.TYPE) || type.similarTo (Short.TYPE)) {
            setType (Types.fromClass (Integer.TYPE));
        }
    }

    @Override
    public void compile () {
        child.compile ();
        // -> T

        /* Negate the value. */
        int op = type.getOpcode (Opcodes.INEG);
        compilationContext.getMethodVisitor ().visitInsn (op);
        // T -> T
    }

}
