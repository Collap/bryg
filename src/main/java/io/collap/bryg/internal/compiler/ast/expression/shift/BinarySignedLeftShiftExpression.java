package io.collap.bryg.internal.compiler.ast.expression.shift;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

public class BinarySignedLeftShiftExpression extends BinaryShiftExpression {

    public BinarySignedLeftShiftExpression (CompilationContext compilationContext, BrygParser.BinaryShiftExpressionContext ctx) {
        super (compilationContext, ctx);
    }

    public BinarySignedLeftShiftExpression (CompilationContext compilationContext, Expression left, Expression right, int line) {
        super (compilationContext, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.ISHL;
    }

}
