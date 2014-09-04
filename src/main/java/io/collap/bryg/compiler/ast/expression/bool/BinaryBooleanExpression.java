package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

/* This also would have to extend BinaryExpression, but multiple
   inheritance is not permitted in Java. */

public abstract class BinaryBooleanExpression extends BooleanExpression {

    protected Expression left;
    protected Expression right;

    protected BinaryBooleanExpression (Context context, BrygParser.ExpressionContext left, BrygParser.ExpressionContext right) {
        super (context);

        StandardVisitor ptv = context.getParseTreeVisitor ();
        this.left = (Expression) ptv.visit (left);
        this.right = (Expression) ptv.visit (right);
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        left.print (out, depth + 1);
        right.print (out, depth + 1);
    }

}
