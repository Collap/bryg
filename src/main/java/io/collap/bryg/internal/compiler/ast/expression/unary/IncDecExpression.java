package io.collap.bryg.internal.compiler.ast.expression.unary;

import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.DummyExpression;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.ast.expression.arithmetic.BinaryAdditionExpression;
import io.collap.bryg.internal.compiler.ast.expression.literal.IntegerLiteralExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.internal.compiler.util.OperationUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

// TODO: Omit DUP if the value of the expression is not needed (in case of discards, for example).
/* The bytecode for

        ++i

   currently looks like this:

        DUP
        ISTORE i
        POP

   Which is, obviously, suboptimal.
 */

public class IncDecExpression extends Expression {

    private Expression get;
    private Expression set;
    private Expression action;

    public IncDecExpression (Context context, BrygParser.ExpressionContext childCtx,
                             boolean isIncrement, boolean isPrefix, int line) {
        super (context);
        setLine (line);

        if (childCtx instanceof BrygParser.VariableExpressionContext) {
            VariableExpression getExpr = new VariableExpression (context,
                    (BrygParser.VariableExpressionContext) childCtx, AccessMode.get);
            Variable variable = getExpr.getVariable ();
            setType (variable.getType ());

            // TODO: Special case for integer variables (IINC).
            int amount;
            if (isIncrement) {
                amount = 1;
            }else { /* decrement */
                amount = -1;
            }

            action = new BinaryAdditionExpression (context,
                    new DummyExpression (context, type, getLine ()), /* The get expression is already compiled before! */
                    new IntegerLiteralExpression (context, amount, getLine ()),
                    getLine ());

            Expression valueExpr;
            if (isPrefix) {
                valueExpr = new Expression (context) {
                    @Override
                    public void compile () {
                        action.compile ();
                        compileDuplicate ();
                    }
                };
            }else { /* postfix */
                valueExpr = new Expression (context) {
                    @Override
                    public void compile () {
                        compileDuplicate ();
                        action.compile ();
                    }
                };
            }

            get = getExpr;
            set = new VariableExpression (context, getLine (), variable, AccessMode.set, valueExpr);
        }else {
            throw new BrygJitException ("Increment and decrement expressions are currently only supported for " +
                    "variables.", line);
        }
    }

    @Override
    public void compile () {
        get.compile ();
        // -> T

        set.compile ();
        // T -> T

        // Stack: T
    }

    private void compileDuplicate () {
        OperationUtil.compileDup (context.getMethodVisitor (), type);
    }

}
