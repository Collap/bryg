package io.collap.bryg.internal.compiler.ast.expression.shift;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

public class BinaryUnsignedRightShiftExpression extends BinaryShiftExpression {

    public BinaryUnsignedRightShiftExpression(CompilationContext compilationContext, BrygParser.BinaryShiftExpressionContext ctx) {
        super(compilationContext, ctx);
    }

    public BinaryUnsignedRightShiftExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, left, right, line);
    }

    @Override
    protected int getOpcode() {
        return Opcodes.IUSHR;
    }

}
