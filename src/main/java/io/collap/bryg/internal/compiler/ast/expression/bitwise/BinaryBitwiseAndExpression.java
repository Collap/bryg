package io.collap.bryg.internal.compiler.ast.expression.bitwise;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

// TODO: Reduce footprint by removing these simple classes?

public class BinaryBitwiseAndExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseAndExpression (CompilationContext compilationContext, BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        super (compilationContext, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseAndExpression (CompilationContext compilationContext, Expression left, Expression right, int line) {
        super (compilationContext, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IAND;
    }

}
