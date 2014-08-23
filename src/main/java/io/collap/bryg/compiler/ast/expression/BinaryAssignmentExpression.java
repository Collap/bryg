package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

public class BinaryAssignmentExpression extends BinaryExpression {

    // TODO: Allow the following: val byte b = 42
    // TODO: Make the indication of casting easier for these scenarios: (int) (value / 0.5) ; Where value is an int.

    /**
     * The right expression does not need to be compiled if the left expression takes care of it.
     */
    private boolean compileRight;

    public BinaryAssignmentExpression (StandardVisitor visitor, BrygParser.BinaryAssignmentExpressionContext ctx) {
        super (visitor, ctx.getStart ().getLine ());
        setType (new Type (Void.TYPE)); // TODO: Implement as proper expression?

        /* Get operator. */
        int operator = ctx.op.getType ();

        /* Evaluate left. */
        Type expectedType = null;
        BrygParser.ExpressionContext leftCtx = ctx.expression (0);
        Expression leftGet = null; /* Set when the operator is used. */
        if (leftCtx instanceof BrygParser.VariableExpressionContext) {
            // TODO: Use new variable constructor!
            BrygParser.VariableExpressionContext variableCtx = (BrygParser.VariableExpressionContext) leftCtx;
            String variableName = IdHelper.idToString (variableCtx.variable ().id ());
            int variableLine = ctx.getStart ().getLine ();
            Variable variable = visitor.getCurrentScope ().getVariable (variableName);
            if (variable == null) {
                throw new BrygJitException ("Variable " + variableName + " not found!", variableLine);
            }
            left = new VariableExpression (visitor, variable, AccessMode.set, variableLine);
            expectedType = variable.getType ();

            if (operator != BrygLexer.ASSIGN) {
                leftGet = new VariableExpression (visitor, variable, AccessMode.get, variableLine);
            }
        }else if (leftCtx instanceof BrygParser.AccessExpressionContext) {
            BrygParser.AccessExpressionContext accessCtx = (BrygParser.AccessExpressionContext) leftCtx;
            try {
                AccessExpression accessExpression = new AccessExpression (visitor, accessCtx, AccessMode.set);
                expectedType = new Type (accessExpression.getField ().getType ());
                left = accessExpression;

                if (operator != BrygLexer.ASSIGN) {
                    leftGet = new AccessExpression (visitor, accessCtx, AccessMode.get);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace (); // TODO: Log properly.
                throw new BrygJitException ("The field to set either does not exist or is not visible!", getLine ());
            }
        }else {
            throw new BrygJitException ("The assignment expression does not include a assignable left hand expression!", getLine ());
        }

        right = (Expression) visitor.visit (ctx.expression (1));

        /* Handle +=, -=, etc. cases. */
        if (operator != BrygLexer.ASSIGN) {
            switch (operator) {
                case BrygLexer.ADD_ASSIGN:
                    right = new BinaryAdditionExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.SUB_ASSIGN:
                    right = new BinarySubtractionExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.MUL_ASSIGN:
                    right = new BinaryMultiplicationExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.DIV_ASSIGN:
                    right = new BinaryDivisionExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.REM_ASSIGN:
                    right = new BinaryRemainderExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.BAND_ASSIGN:
                    right = new BinaryBitwiseAndExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.BXOR_ASSIGN:
                    right = new BinaryBitwiseXorExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.BOR_ASSIGN:
                    right = new BinaryBitwiseOrExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.SIG_LSHIFT_ASSIGN:
                    right = new BinarySignedLeftShiftExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.SIG_RSHIFT_ASSIGN:
                    right = new BinarySignedRightShiftExpression (visitor, leftGet, right, getLine ());
                    break;
                case BrygLexer.UNSIG_RSHIFT_ASSIGN:
                    right = new BinaryUnsignedRightShiftExpression (visitor, leftGet, right, getLine ());
                    break;
                default:
                    throw new BrygJitException ("Operator " + operator + " is not supported in assignments!", getLine ());
            }
        }

        /* Possible coercion. */
        if (!expectedType.equals (right.getType ())) {
            Type targetType = CoercionHelper.getTargetType (expectedType, right.getType (), getLine ());
            if (!expectedType.equals (targetType)) {
                throw new BrygJitException ("Coercion for assignment failed, please cast manually from '" +
                        targetType + "' to '" + expectedType + "'.", getLine ());
            }

            /* Add cast expression on top. */
            right = new CastExpression (visitor, targetType, right, getLine ());
        }

        /* In this case 'left' takes care of compiling 'right'. */
        if (left instanceof AccessExpression) {
            ((AccessExpression) left).setSetFieldExpression (right);
            compileRight = false;
        }else {
            compileRight = true;
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
