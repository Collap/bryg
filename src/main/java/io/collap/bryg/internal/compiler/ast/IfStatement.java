package io.collap.bryg.internal.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.bool.BooleanExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static bryg.org.objectweb.asm.Opcodes.GOTO;

public class IfStatement extends Node {

    private BooleanExpression condition;
    private Node ifStatementOrBlock;
    private Node elseStatementOrBlock;

    public IfStatement (CompilationContext compilationContext, BrygParser.IfStatementContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        StandardVisitor ptv = compilationContext.getParseTreeVisitor ();

        Expression conditionOrExpression = (Expression) ptv.visit (ctx.expression ());
        if (conditionOrExpression instanceof BooleanExpression) {
            condition = (BooleanExpression) conditionOrExpression;
        }else {
            throw new BrygJitException ("The condition is not a BooleanExpression", getLine ());
        }

        BrygParser.StatementContext ifStatementCtx = ctx.statement ();
        if (ifStatementCtx != null) {
            ifStatementOrBlock = ptv.visitStatement (ifStatementCtx);
        }else {
            ifStatementOrBlock = ptv.visitBlock (ctx.block ());
        }

        BrygParser.StatementOrBlockContext elseCtx = ctx.statementOrBlock ();
        if (elseCtx != null) {
            elseStatementOrBlock = ptv.visitStatementOrBlock (elseCtx);
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        boolean elseExists = elseStatementOrBlock != null;
        Label afterIf = new Label ();
        Label falseNext;
        if (elseExists) {
            falseNext = new Label ();
        }else {
            falseNext = afterIf;
        }

        Label trueNext = new Label ();
        condition.compile (falseNext, trueNext, true);

        /* if block/statement. */
        mv.visitLabel (trueNext);
        ifStatementOrBlock.compile ();

        /* else block/statement. */
        if (elseExists) {
            mv.visitJumpInsn (GOTO, afterIf); // Belongs to if block!
            mv.visitLabel (falseNext);
            elseStatementOrBlock.compile ();
        }

        mv.visitLabel (afterIf);
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        condition.print (out, depth + 1);
        ifStatementOrBlock.print (out, depth + 1);
        if (elseStatementOrBlock != null) {
            elseStatementOrBlock.print (out, depth + 1);
        }
    }

}
