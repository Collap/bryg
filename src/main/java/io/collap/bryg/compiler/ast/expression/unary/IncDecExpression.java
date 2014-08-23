package io.collap.bryg.compiler.ast.expression.unary;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.ast.expression.DummyExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.ast.expression.arithmetic.BinaryAdditionExpression;
import io.collap.bryg.compiler.ast.expression.literal.IntegerLiteralExpression;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.helper.OperationHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class IncDecExpression extends Expression {

    private boolean isIncrement; /* Otherwise: decrement. */
    private boolean isPrefix; /* Otherwise: postfix */

    private Expression get;
    private Expression set;
    private Expression action;

    public IncDecExpression (StandardVisitor visitor, BrygParser.ExpressionContext childCtx,
                             boolean isIncrement, boolean isPrefix, int line) {
        super (visitor);
        this.isIncrement = isIncrement;
        this.isPrefix = isPrefix;
        setLine (line);

        if (childCtx instanceof BrygParser.VariableExpressionContext) {
            VariableExpression getExpr = new VariableExpression (visitor,
                    (BrygParser.VariableExpressionContext) childCtx, AccessMode.get);
            Variable variable = getExpr.getVariable ();
            setType (variable.getType ());

            get = getExpr;
            set = new VariableExpression (visitor, variable, AccessMode.set, getLine ());

            // TODO: Special case for integer variables (IINC).
            int amount;
            if (isIncrement) {
                amount = 1;
            }else { /* decrement */
                amount = -1;
            }

            action = new BinaryAdditionExpression (visitor,
                    new DummyExpression (visitor, type, getLine ()), /* The get expression is already compiled before! */
                    new IntegerLiteralExpression (visitor, amount, getLine ()),
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
        OperationHelper.compileDup (visitor.getMethod (), type);
    }

}