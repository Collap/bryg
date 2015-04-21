package io.collap.bryg.internal.compiler.ast.expression.bool;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

public class RelationalBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public RelationalBinaryBooleanExpression(CompilationContext compilationContext, BrygParser.BinaryRelationalExpressionContext ctx) {
        super(compilationContext, ctx.expression(0), ctx.expression(1), ctx.op.getType());
    }

}
