package io.collap.bryg.internal.compiler.ast.expression.arithmetic;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

// TODO: Reduce footprint by removing these simple classes?

public class BinaryRemainderExpression extends BinaryArithmeticExpression {

    public BinaryRemainderExpression(CompilationContext compilationContext, BrygParser.ExpressionContext leftCtx,
                                     BrygParser.ExpressionContext rightCtx) {
        super(compilationContext, leftCtx, rightCtx);
    }

    public BinaryRemainderExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, left, right, line);
    }

    @Override
    protected int getOpcode() {
        return Opcodes.IREM;
    }

}
