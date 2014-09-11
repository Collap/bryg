package io.collap.bryg.compiler.ast.control;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.bool.BooleanExpression;
import io.collap.bryg.compiler.ast.expression.bool.ExpressionBooleanExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.GOTO;

public class WhileStatement extends Node {

    private Expression condition;
    private Node body;

    public WhileStatement (Context context, BrygParser.WhileStatementContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        StandardVisitor ptv = context.getParseTreeVisitor ();
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
        BrygMethodVisitor mv = context.getMethodVisitor ();

        Label conditionLabel = new Label ();
        Label endLabel = new Label ();

        /* Condition. */
        mv.visitLabel (conditionLabel);
        BooleanExpression booleanExpression;
        if (condition instanceof BooleanExpression) {
            booleanExpression = ((BooleanExpression) condition);
        }else {
            booleanExpression = new ExpressionBooleanExpression (context, condition);
        }
        booleanExpression.compile (endLabel, null, true);

        /* Body. */
        body.compile ();
        mv.visitJumpInsn (GOTO, conditionLabel);

        mv.visitLabel (endLabel);
    }

}
