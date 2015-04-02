package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

public abstract class BinaryExpression extends Expression {

    protected Expression left;
    protected Expression right;

    /**
     * Does <b>not</b> set the type.
     * Sets the line.
     * Visits and sets the left and right expressions.
     */
    protected BinaryExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                BrygParser.ExpressionContext rightCtx) {
        super (context);
        setLine (leftCtx.getStart ().getLine ());

        StandardVisitor ptv = context.getParseTreeVisitor ();
        left = (Expression) ptv.visit (leftCtx);
        right = (Expression) ptv.visit (rightCtx);
        if (left == null || right == null) {
            throw new BrygJitException ("Left or right is null: " + left + ", " + right, getLine ());
        }
    }

    /**
     * This constructor <b>only</b> sets the line.
     */
    protected BinaryExpression (Context context, int line) {
        super (context);
        setLine (line);
    }

    /**
     * Does <b>not</b> set the type.
     */
    protected BinaryExpression (Context context, Expression left, Expression right, int line) {
        super (context);
        setLine (line);
        this.left = left;
        this.right = right;
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        left.print (out, depth + 1);
        right.print (out, depth + 1);
    }

}
