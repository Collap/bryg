package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.io.PrintStream;

public abstract class BinaryExpression extends Expression {

    protected Expression left;
    protected Expression right;

    /**
     * Does <b>not</b> set the type.
     * Sets the line.
     * Visits and sets the left and right expressions.
     */
    protected BinaryExpression(CompilationContext compilationContext, BrygParser.ExpressionContext leftCtx,
                               BrygParser.ExpressionContext rightCtx) {
        super(compilationContext, leftCtx.getStart().getLine());

        StandardVisitor ptv = compilationContext.getParseTreeVisitor();
        @Nullable Expression left = (Expression) ptv.visit(leftCtx);
        @Nullable Expression right = (Expression) ptv.visit(rightCtx);
        if (left == null || right == null) { // TODO: Is this ever reached? IntelliJ says no. ANTLR documentation insufficient.
            throw new BrygJitException("Left or right is null: " + left + ", " + right, getLine());
        }

        this.left = left;
        this.right = right;
    }

    /**
     * Does <b>not</b> set the type.
     */
    protected BinaryExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, line);
        this.left = left;
        this.right = right;
    }

    @Override
    public void print(PrintStream out, int depth) {
        super.print(out, depth);
        left.print(out, depth + 1);
        right.print(out, depth + 1);
    }

}
