package io.collap.bryg.compiler.ast.expression.unary;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.ast.expression.DummyExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.ast.expression.arithmetic.BinaryAdditionExpression;
import io.collap.bryg.compiler.ast.expression.literal.IntegerLiteralExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.util.OperationUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class IncDecExpression extends Expression {

    private boolean isIncrement; /* Otherwise: decrement. */
    private boolean isPrefix; /* Otherwise: postfix */

    private Expression get;
    private Expression set;
    private Expression action;

    public IncDecExpression (Context context, BrygParser.ExpressionContext childCtx,
                             boolean isIncrement, boolean isPrefix, int line) {
        super (context);
        this.isIncrement = isIncrement;
        this.isPrefix = isPrefix;
        setLine (line);

        if (childCtx instanceof BrygParser.VariableExpressionContext) {
            VariableExpression getExpr = new VariableExpression (context,
                    (BrygParser.VariableExpressionContext) childCtx, AccessMode.get);
            Variable variable = getExpr.getVariable ();
            setType (variable.getType ());

            get = getExpr;
            set = new VariableExpression (context, variable, AccessMode.set, getLine ());

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
        }else {
            throw new BrygJitException ("Increment and decrement expressions are currently only supported for " +
                    "variables.", line);
        }
    }

    @Override
    public void compile () {
        get.compile ();
        // -> T

        if (isPrefix) {
            action.compile ();
            compileDuplicate ();
        }else { /* postfix */
            compileDuplicate ();
            action.compile ();
        }
        // T -> T, T

        set.compile ();
        // T ->

        // Stack: T
    }

    private void compileDuplicate () {
        OperationUtil.compileDup (context.getMethodVisitor (), type);
    }

}
