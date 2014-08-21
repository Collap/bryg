package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.ast.expression.arithmetic.BinaryAdditionExpression;
import io.collap.bryg.compiler.expression.Operator;
import io.collap.bryg.compiler.expression.Operators;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class BinaryAssignmentExpression extends Expression {

    // TODO: Support -=, *=, etc.

    private Expression left;
    private Expression right;

    /**
     * The right expression does not need to be compiled if the left expression takes care of it.
     */
    private boolean compileRight;

    public BinaryAssignmentExpression (StandardVisitor visitor, BrygParser.BinaryAssignmentExpressionContext ctx) {
        super (visitor);
        setType (new Type (Void.TYPE)); // TODO: Implement as proper expression?
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

            compileRight = true;
        }else if (leftCtx instanceof BrygParser.AccessExpressionContext) {
            BrygParser.AccessExpressionContext accessCtx = (BrygParser.AccessExpressionContext) leftCtx;
            try {
                left = new AccessExpression (visitor, accessCtx, AccessMode.set);
                if (operator != null) {
                    leftGet = new AccessExpression (visitor, accessCtx, AccessMode.get);
                }
                compileRight = false;
            } catch (NoSuchFieldException e) {
                e.printStackTrace (); // TODO: Log properly.
                throw new BrygJitException ("The field to set either does not exist or is not visible!", getLine ());
            }
        }else {
            throw new BrygJitException ("The assignment expression does not include a assignable left hand expression!", getLine ());
        }

        right = (Expression) visitor.visit (ctx.expression (1));
        if (operator != null) {
            switch (operator) {
                case addition:
                    right = new BinaryAdditionExpression (visitor, leftGet, right, getLine ());
                    break;
                default:
                    throw new BrygJitException ("Operator " + operator + " is currently not supported!", getLine ());
            }
        }

        /* In this case 'left' takes care of compiling 'right'. */
        if (left instanceof AccessExpression) {
            ((AccessExpression) left).setSetFieldExpression (right);
        }
    }

    @Override
    public void compile () {
        if (compileRight) {
            right.compile ();
            // -> value
        }

        left.compile ();
        // value ->
    }

}
