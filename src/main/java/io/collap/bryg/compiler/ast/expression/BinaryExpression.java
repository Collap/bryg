package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
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
    protected BinaryExpression (StandardVisitor visitor, BrygParser.ExpressionContext leftCtx,
                                BrygParser.ExpressionContext rightCtx) {
        super (visitor);
        setLine (leftCtx.getStart ().getLine ());
        left = (Expression) visitor.visit (leftCtx);
        right = (Expression) visitor.visit (rightCtx);
        if (left == null || right == null) {
            throw new BrygJitException ("Left or right is null: " + left + ", " + right, getLine ());
        }
    }

    /**
     * This constructor <b>only</b> sets the line.
     */
    protected BinaryExpression (StandardVisitor visitor, int line) {
        super (visitor);
        setLine (line);
    }

    /**
     * Does <b>not</b> set the type.
     */
    protected BinaryExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor);
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