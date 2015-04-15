package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nonnull;

public class ValueLiteralExpression<T> extends Expression {

    protected T value;

    public ValueLiteralExpression(CompilationContext compilationContext, int line, Class<T> type, T value) {
        super(compilationContext, line);
        setType(Types.fromClass(type));
        this.value = value;
    }

    @Override
    public void compile() {
        checkExists();
        compilationContext.getMethodVisitor().visitLdcInsn(value);
    }

    private void checkExists() {
        if (value == null) {
            throw new BrygJitException("Value of literal has not been set!", getLine());
        }
    }

    @Override
    public @Nonnull Object getConstantValue() {
        checkExists();
        return value;
    }

}
