package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.compiler.util.FunctionUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MethodCallExpression extends Expression {

    private class CoercionResult {
        public Method method;
        public List<Expression> arguments;

        private CoercionResult (Method method, List<Expression> arguments) {
            this.method = method;
            this.arguments = arguments;
        }
    }

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
        findMethod (IdUtil.idToString (ctx.functionCall ().id ()), objectType);
        setType (new Type (method.getReturnType ()));
    }

    private void findMethod (String methodName, Class<?> objectType) {
        Method[] methods = objectType.getMethods ();
        List<CoercionResult> results = findMethods (methodName, methods, false);

        /* If method was not found, try with coercion. */
        if (results.size () == 0) {
            System.out.println ("Try with coercion!");
            results = findMethods (methodName, methods, true);
        }

        if (results.size () == 0) {
            throw new BrygJitException ("Method " + methodName + " could not be found for the argument types.", getLine ());
        }else if (results.size () > 1) {
            // TODO: List coercion results.
            throw new BrygJitException ("Coercion of argument types with method " + methodName + " leads to ambiguous results.", getLine ());
        }

        CoercionResult result = results.get (0);
        method = result.method;
        if (result.arguments != null) {
            argumentExpressions = result.arguments;
        }
    }

    private List<CoercionResult> findMethods (String methodName, Method[] methods, boolean coerce) {
        List<CoercionResult> results = new ArrayList<> ();
        for (Method method : methods) {
            if (method.getName ().equals (methodName)) {
                CoercionResult coercionResult = checkMethod (method, coerce);
                if (coercionResult != null) {
                    results.add (coercionResult);
                }
            }
        }
        return results;
    }

    private @Nullable CoercionResult checkMethod (Method method, boolean coerce) {
        final Class<?>[] parameterTypes = method.getParameterTypes ();
        final int numParams = parameterTypes.length;

        if (argumentExpressions.size () != numParams) return null;

        List<Expression> arguments = null;
        if (coerce) {
            arguments = new ArrayList<> (numParams);
        }

        /* Check if the types match. */
        for (int i = 0; i < numParams; ++i) {
            Class<?> paramType = parameterTypes[i];
            Expression expression = argumentExpressions.get (i);
            if (!expression.getType ().similarTo (paramType)) { /* Check if the exact types match. */
                if (coerce) {
                    Expression coercionExpression = CoercionUtil.tryUnaryCoercion (context, expression, new Type (paramType));
                    if (coercionExpression == null) {
                        return null;
                    }
                    arguments.add (coercionExpression);
                }else {
                    return null;
                }
            }else {
                if (coerce) {
                    arguments.add (expression);
                }
            }
        }

        return new CoercionResult (method, arguments);
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
