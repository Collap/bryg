package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryBooleanExpression extends BooleanExpression {

    protected Expression left;
    protected Expression right;

    protected BinaryBooleanExpression (StandardVisitor visitor, BrygParser.ExpressionContext left, BrygParser.ExpressionContext right) {
        super (visitor);
        this.left = (Expression) visitor.visit (left);
        this.right = (Expression) visitor.visit (right);
    }

}
