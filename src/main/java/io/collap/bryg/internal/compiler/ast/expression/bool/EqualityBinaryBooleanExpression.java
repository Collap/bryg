package io.collap.bryg.internal.compiler.ast.expression.bool;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

public class EqualityBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public EqualityBinaryBooleanExpression (CompilationContext compilationContext, BrygParser.BinaryEqualityExpressionContext ctx) {
        super (compilationContext, ctx.expression (0), ctx.expression (1), ctx.op.getType ());
        setLine (ctx.getStart ().getLine ());
    }

}
