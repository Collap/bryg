package io.collap.bryg.internal.compiler.ast.expression.bool;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

public class EqualityBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public EqualityBinaryBooleanExpression (Context context, BrygParser.BinaryEqualityExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1), ctx.op.getType ());
        setLine (ctx.getStart ().getLine ());
    }

}
