package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.Types;

import static bryg.org.objectweb.asm.Opcodes.ACONST_NULL;

public class ObjectLiteralExpression extends LiteralExpression {

    public ObjectLiteralExpression (Context context, Object value, int line) {
        super (context, line);
        setType (Types.fromClass (Object.class));

        this.value = value;
    }

    @Override
    public void compile () {
        context.getMethodVisitor ().visitInsn (ACONST_NULL);
    }

    @Override
    /**
     * This has to be overridden, because null is a valid constant value.
     */
    public boolean isConstant () {
        return true;
    }

}
