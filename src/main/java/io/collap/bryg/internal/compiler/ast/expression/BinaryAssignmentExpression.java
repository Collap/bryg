package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

public class BinaryAssignmentExpression extends BinaryExpression {

    // TODO: Make the indication of casting easier for these scenarios: (int) (value / 0.5) ; Where value is an int. (Maybe fix)

    /**
     * The right expression does not need to be compiled if the left expression takes care of it.
     */
    private boolean compileRight;

    public BinaryAssignmentExpression (CompilationContext compilationContext, BrygParser.BinaryAssignmentExpressionContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Void.TYPE)); // TODO: Implement as proper expression?

        /* Get operator. */
        int operator = ctx.op.getType ();

        /* Evaluate left. */
        Type expectedType = null;
        BrygParser.ExpressionContext leftCtx = ctx.expression (0);
        Expression leftGet = null; /* Set when the operator is used. */
        if (leftCtx instanceof BrygParser.VariableExpressionContext) {
            // TODO: Use new variable expression constructor!
            BrygParser.VariableExpressionContext variableCtx = (BrygParser.VariableExpressionContext) leftCtx;
            String variableName = IdUtil.idToString (variableCtx.variable ().id ());
            int variableLine = ctx.getStart ().getLine ();
            Variable variable = compilationContext.getCurrentScope ().getVariable (variableName);
            if (variable == null) {
                throw new BrygJitException ("Variable '" + variableName + "' not found.", variableLine);
            }

            if (!variable.isMutable ()) {
                throw new BrygJitException ("Variable '" + variableName + "' is not mutable.", variableLine);
            }

            left = new VariableExpression (compilationContext, variableLine,variable, AccessMode.set);
            expectedType = variable.getType ();

            if (operator != BrygLexer.ASSIGN) {
                leftGet = new VariableExpression (compilationContext, variableLine, variable, AccessMode.get);
            }
        }else if (leftCtx instanceof BrygParser.AccessExpressionContext) {
            BrygParser.AccessExpressionContext accessCtx = (BrygParser.AccessExpressionContext) leftCtx;
            try {
                AccessExpression accessExpression = new AccessExpression (compilationContext, accessCtx, AccessMode.set);
                expectedType = Types.fromClass (accessExpression.getField ().getType ());
                left = accessExpression;

                if (operator != BrygLexer.ASSIGN) {
                    leftGet = new AccessExpression (compilationContext, accessCtx, AccessMode.get);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace (); // TODO: Log properly. (Fix with Improved Error Handling)
                throw new BrygJitException ("The field to set either does not exist or is not visible!", getLine ());
            }
        }else {
            throw new BrygJitException ("The assignment expression does not include a assignable left hand expression!", getLine ());
        }

        right = (Expression) compilationContext.getParseTreeVisitor ().visit (ctx.expression (1));

        /* Handle +=, -=, etc. cases. */
        if (operator != BrygLexer.ASSIGN) {
            switch (operator) {
                case BrygLexer.ADD_ASSIGN:
                    right = new BinaryAdditionExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.SUB_ASSIGN:
                    right = new BinarySubtractionExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.MUL_ASSIGN:
                    right = new BinaryMultiplicationExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.DIV_ASSIGN:
                    right = new BinaryDivisionExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.REM_ASSIGN:
                    right = new BinaryRemainderExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.BAND_ASSIGN:
                    right = new BinaryBitwiseAndExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.BXOR_ASSIGN:
                    right = new BinaryBitwiseXorExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.BOR_ASSIGN:
                    right = new BinaryBitwiseOrExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.SIG_LSHIFT_ASSIGN:
                    right = new BinarySignedLeftShiftExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.SIG_RSHIFT_ASSIGN:
                    right = new BinarySignedRightShiftExpression (compilationContext, leftGet, right, getLine ());
                    break;
                case BrygLexer.UNSIG_RSHIFT_ASSIGN:
                    right = new BinaryUnsignedRightShiftExpression (compilationContext, leftGet, right, getLine ());
                    break;
                default:
                    throw new BrygJitException ("Operator " + operator + " is not supported in assignments!", getLine ());
            }
        }

        /* Possible coercion. */
        if (!expectedType.similarTo (right.getType ())) {
            right = CoercionUtil.applyUnaryCoercion (compilationContext, right, expectedType);
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
