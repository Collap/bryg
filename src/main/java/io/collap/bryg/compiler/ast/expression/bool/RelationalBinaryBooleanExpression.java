package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.expression.Operators;
import io.collap.bryg.parser.BrygParser;

public class RelationalBinaryBooleanExpression extends OperatorBinaryBooleanExpression {

    public RelationalBinaryBooleanExpression (StandardVisitor visitor, BrygParser.BinaryRelationalExpressionContext ctx) {
        super (visitor, ctx.expression (0), ctx.expression (1), Operators.fromString (ctx.getChild (1).getText ()));
    }

}
