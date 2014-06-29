package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.expression.bool.ExpressionBooleanExpression;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.ast.expression.bool.BooleanExpression;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.GOTO;

public class IfExpression extends Expression {

    private BooleanExpression condition;
    private Node ifStatementOrBlock;
    private Node elseStatementOrBlock;

    public IfExpression (StandardVisitor visitor, BrygParser.IfExpressionContext ctx) {
        super (visitor);
        setLine (ctx.getStart ().getLine ());
        setType (new Type (Void.TYPE)); // TODO: Implement if as a proper expression?

        Expression conditionOrExpression = (Expression) visitor.visit (ctx.expression ());
        if (conditionOrExpression instanceof BooleanExpression) {
            condition = (BooleanExpression) conditionOrExpression;
        }else {
            condition = new ExpressionBooleanExpression (visitor, conditionOrExpression);
        }
        ifStatementOrBlock = visitor.visitStatementOrBlock (ctx.statementOrBlock (0));

        BrygParser.StatementOrBlockContext elseCtx = ctx.statementOrBlock (1);
        if (elseCtx != null) {
            elseStatementOrBlock = visitor.visitStatementOrBlock (elseCtx);
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

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
        method.visitLabelInSameFrame (trueNext);
        ifStatementOrBlock.compile ();

        /* else block/statement. */
        if (elseExists) {
            method.visitJumpInsn (GOTO, afterIf); // Belongs to if block!
            method.visitLabelInSameFrame (falseNext);
            elseStatementOrBlock.compile ();
        }

        method.visitLabelInSameFrame (afterIf);
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
