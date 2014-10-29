package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.compiler.util.FunctionUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MethodCallExpression extends Expression {

    private Expression operandExpression;
    private List<Expression> argumentExpressions;
    private Method method;

    public MethodCallExpression (Context context, BrygParser.MethodCallExpressionContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        operandExpression = (Expression) context.getParseTreeVisitor ().visit (ctx.expression ());

        Class<?> objectType = operandExpression.getType ().getJavaType ();
        if (objectType.isPrimitive ()) {
            throw new BrygJitException ("Methods can not be invoked on primitives.", getLine ());
        }

        String methodName = IdUtil.idToString (ctx.functionCall ().id ());

        /* Init argument expressions. */
        List<ArgumentExpression> arguments = FunctionUtil.parseArgumentList (context, ctx.functionCall ().argumentList ());
        argumentExpressions = new ArrayList<> (arguments.size ());

        /* Validate arguments. */
        for (ArgumentExpression argument : arguments) {
            /* Check that no argument is named or has a predicate. */
            if (argument.getName () != null) {
                throw new BrygJitException ("Named arguments are not supported with method calls.", getLine ());
            }

            if (argument.getPredicate () != null) {
                throw new BrygJitException ("Argument predicates are not supported with method calls.", getLine ());
            }

            argumentExpressions.add (argument);
        }

        /* Find method. */
        Method[] methods = objectType.getMethods ();
        for (Method supposedMethod : methods) {
            if (supposedMethod.getName ().equals (methodName)) {
                Class<?>[] parameterTypes = supposedMethod.getParameterTypes ();
                if (checkParameters (parameterTypes) || checkParametersAndCoerce (parameterTypes)) {
                    method = supposedMethod;
                }
            }
        }

        if (method == null) {
            throw new BrygJitException ("Method " + methodName + " could not be found.", getLine ());
        }

        setType (new Type (method.getReturnType ()));
    }

    private boolean checkParameters (Class<?>[] actualTypes) {
        return checkParametersAndMaybeCoerce (actualTypes, false);
    }

    private boolean checkParametersAndCoerce (Class<?>[] actualTypes) {
        return checkParametersAndMaybeCoerce (actualTypes, true);
    }

    // TODO: Return an error if multiple matches have been found (Fix before 1.0).
    private boolean checkParametersAndMaybeCoerce (Class<?>[] actualTypes, boolean coerce) {
        final int numParams = actualTypes.length;

        if (argumentExpressions.size () != numParams) return false;

        Expression[] coercionExpressions = null;
        if (coerce) {
            coercionExpressions = new Expression[numParams];
        }

        /* Check if the types match. */
        for (int i = 0; i < numParams; ++i) {
            Class<?> actualType = actualTypes[i];
            Expression expression = argumentExpressions.get (i);
            if (!actualType.isAssignableFrom (expression.getType ().getJavaType ())) {
                if (coerce) {
                    Expression coercionExpression = CoercionUtil.tryUnaryCoercion (context, expression, new Type (actualType));
                    if (coercionExpression == null) {
                        return false;
                    }
                    coercionExpressions[i] = coercionExpression;
                }else {
                    return false;
                }
            }
        }

        /* Replace argument expressions with coercion expressions. */
        if (coerce) {
            for (int i = 0; i < numParams; ++i) {
                Expression ce = coercionExpressions[i];
                if (ce != null) {
                    argumentExpressions.set (i, ce);
                }
            }
        }

        return true;
    }

    @Override
    public void compile () {
        Type ownerType = operandExpression.getType ();
        boolean isInterface = ownerType.getJavaType ().isInterface ();

        operandExpression.compile ();
        // -> O

        for (Expression expression : argumentExpressions) {
            expression.compile ();
        }
        // -> A1, A2, ...

        context.getMethodVisitor ().visitMethodInsn (isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
                ownerType.getAsmType ().getInternalName (),
                method.getName (),
                TypeHelper.generateMethodDesc (
                        method.getParameterTypes (),
                        method.getReturnType ()
                ), isInterface);
        // O, A1, A2, ... -> T
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        operandExpression.print (out, depth + 1);
    }

}
