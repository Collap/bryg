package io.collap.bryg.internal.compiler.ast.expression.bitwise;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

public class BinaryBitwiseXorExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseXorExpression (CompilationContext compilationContext, BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        super (compilationContext, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseXorExpression (CompilationContext compilationContext, Expression left, Expression right, int line) {
        super (compilationContext, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IXOR;
    }

}
