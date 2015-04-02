package io.collap.bryg.internal.compiler.ast.expression.bool;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

public class RelationalBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public RelationalBinaryBooleanExpression (Context context, BrygParser.BinaryRelationalExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1), ctx.op.getType ());
        setLine (ctx.getStart ().getLine ());
    }

}
