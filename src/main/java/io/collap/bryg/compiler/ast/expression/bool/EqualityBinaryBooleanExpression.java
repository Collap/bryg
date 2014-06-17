package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.expression.Operators;
import io.collap.bryg.parser.BrygParser;

public class EqualityBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public EqualityBinaryBooleanExpression (RenderVisitor visitor, BrygParser.BinaryEqualityExpressionContext ctx) {
        super (visitor, ctx.expression (0), ctx.expression (1), Operators.fromString (ctx.getChild (1).getText ()));
    }

}
