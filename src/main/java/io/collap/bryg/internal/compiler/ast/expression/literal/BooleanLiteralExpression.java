package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;

public class BooleanLiteralExpression extends ValueLiteralExpression<Boolean> {

    public BooleanLiteralExpression(CompilationContext compilationContext, int line, Boolean value) {
        super(compilationContext, line, Boolean.TYPE, value);
    }

    @Override
    public void compile() {
        /* Convert boolean to int, then load the constant. */
        int intValue = value ? 1 : 0;
        compilationContext.getMethodVisitor().visitLdcInsn(intValue);
    }

}
