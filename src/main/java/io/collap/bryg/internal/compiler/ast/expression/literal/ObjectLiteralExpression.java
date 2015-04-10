package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;

import static bryg.org.objectweb.asm.Opcodes.ACONST_NULL;

public class ObjectLiteralExpression extends LiteralExpression {

    public ObjectLiteralExpression (CompilationContext compilationContext, Object value, int line) {
        super (compilationContext, line);
        setType (Types.fromClass (Object.class));

        this.value = value;
    }

    @Override
    public void compile () {
        compilationContext.getMethodVisitor ().visitInsn (ACONST_NULL);
    }

    @Override
    /**
     * This has to be overridden, because null is a valid constant value.
     */
    public boolean isConstant () {
        return true;
    }

}
