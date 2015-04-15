package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.type.Types;

import static bryg.org.objectweb.asm.Opcodes.ACONST_NULL;

public class NullLiteralExpression extends Expression {

    public NullLiteralExpression(CompilationContext compilationContext, int line) {
        super(compilationContext, line);
        setType(Types.fromClass(Object.class));
    }

    @Override
    public void compile() {
        compilationContext.getMethodVisitor().visitInsn(ACONST_NULL);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

}
