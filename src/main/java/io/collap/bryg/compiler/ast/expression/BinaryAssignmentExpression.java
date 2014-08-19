package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.expression.Operator;
import io.collap.bryg.compiler.expression.Operators;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class BinaryAssignmentExpression extends Expression {

    private Expression left;
    private Expression right;

    public BinaryAssignmentExpression (StandardVisitor visitor, BrygParser.BinaryAssignmentExpressionContext ctx) {
        super (visitor);
        setType (new Type (Void.TYPE)); // TODO: Implement as proper expression.
        setLine (ctx.getStart ().getLine ());

        /* Get operator. */
        Operator operator = null;
        String operatorStr = ctx.getChild (1).getText ();
        if (!operatorStr.equals ("=")) {
            operator = Operators.fromString (operatorStr.substring (0, operatorStr.length () - 1));
        }

        /* Evaluate left. */
        BrygParser.ExpressionContext leftCtx = ctx.expression (0);
        Expression leftGet = null; /* Set when the operator is used. */
        if (leftCtx instanceof BrygParser.VariableExpressionContext) {
            BrygParser.VariableExpressionContext variableCtx = (BrygParser.VariableExpressionContext) leftCtx;
            String variableName = IdHelper.idToString (variableCtx.variable ().id ());
            int variableLine = ctx.getStart ().getLine ();
            Variable variable = visitor.getCurrentScope ().getVariable (variableName);
            if (variable == null) {
                throw new BrygJitException ("Variable " + variableName + " not found!", variableLine);
            }
            left = new VariableExpression (visitor, variable, AccessMode.set, variableLine);

            if (operator != null) {
                leftGet = new VariableExpression (visitor, variable, AccessMode.get, variableLine);
            }
        }else if (leftCtx instanceof BrygParser.AccessExpressionContext) {
            throw new UnsupportedOperationException ("Access expressions are not supported for the left hand expression yet!");
        }else {
            throw new BrygJitException ("The assignment expression does not include a assignable left hand expression!", getLine ());
        }

        right = (Expression) visitor.visit (ctx.expression (1));
        if (operator != null) {
            switch (operator) {
                case addition:
                    right = new BinaryAdditionExpression (visitor, leftGet, right, ctx.expression (1).getStart ().getLine ());
                    break;
                default:
                    throw new BrygJitException ("Operator " + operator + " is not supported!", getLine ());
            }
        }
    }

    @Override
    public void compile () {
        right.compile ();
        // -> value

        left.compile ();
        // value ->
    }

}
