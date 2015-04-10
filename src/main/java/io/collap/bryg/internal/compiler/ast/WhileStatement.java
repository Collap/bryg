package io.collap.bryg.internal.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.bool.BooleanExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.GOTO;

public class WhileStatement extends Node {

    private Expression condition;
    private Node body;

    public WhileStatement (CompilationContext compilationContext, BrygParser.WhileStatementContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        StandardVisitor ptv = compilationContext.getParseTreeVisitor ();
        condition = (Expression) ptv.visit (ctx.condition);

        BrygParser.BlockContext blockCtx = ctx.block ();
        if (blockCtx != null) {
            body = ptv.visitBlock (blockCtx);
        }else {
            body = ptv.visitStatement (ctx.statement ());
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        Label conditionLabel = new Label ();
        Label endLabel = new Label ();

        /* Condition. */
        mv.visitLabel (conditionLabel);
        BooleanExpression booleanExpression;
        if (condition instanceof BooleanExpression) {
            booleanExpression = ((BooleanExpression) condition);
        }else {
            throw new BrygJitException ("The condition is not a BooleanExpression", getLine ());
        }
        booleanExpression.compile (endLabel, null, true);

        /* Body. */
        body.compile ();
        mv.visitJumpInsn (GOTO, conditionLabel);

        mv.visitLabel (endLabel);
    }

}
