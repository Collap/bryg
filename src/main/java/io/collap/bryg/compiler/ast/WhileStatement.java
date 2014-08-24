package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.bool.BooleanExpression;
import io.collap.bryg.compiler.ast.expression.bool.ExpressionBooleanExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import static org.objectweb.asm.Opcodes.*;

public class WhileStatement extends Node {

    private Expression condition;
    private Node body;

    public WhileStatement (StandardVisitor visitor, BrygParser.WhileStatementContext ctx) {
        super (visitor);
        setLine (ctx.getStart ().getLine ());
        condition = (Expression) visitor.visit (ctx.condition);

        BrygParser.BlockContext blockCtx = ctx.block ();
        if (blockCtx != null) {
            body = visitor.visitBlock (blockCtx);
        }else {
            body = visitor.visitStatement (ctx.statement ());
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        Label conditionLabel = new Label ();
        Label endLabel = new Label ();

        /* Condition. */
        method.visitLabel (conditionLabel);
        BooleanExpression booleanExpression;
        if (condition instanceof BooleanExpression) {
            booleanExpression = ((BooleanExpression) condition);
        }else {
            booleanExpression = new ExpressionBooleanExpression (visitor, condition);
        }
        booleanExpression.compile (endLabel, null, true);

        /* Body. */
        body.compile ();
        method.visitJumpInsn (GOTO, conditionLabel);

        method.visitLabel (endLabel);
    }

}
