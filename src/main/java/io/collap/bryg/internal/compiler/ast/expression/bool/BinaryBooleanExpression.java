package io.collap.bryg.internal.compiler.ast.expression.bool;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

/* This also would have to extend BinaryExpression, but multiple
   inheritance is not permitted in Java. */

public abstract class BinaryBooleanExpression extends BooleanExpression {

    protected Expression left;
    protected Expression right;

    protected BinaryBooleanExpression (CompilationContext compilationContext, BrygParser.ExpressionContext left, BrygParser.ExpressionContext right) {
        super (compilationContext);

        StandardVisitor ptv = compilationContext.getParseTreeVisitor ();
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
