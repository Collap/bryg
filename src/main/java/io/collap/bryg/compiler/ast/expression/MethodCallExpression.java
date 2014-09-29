package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
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
    private List<ArgumentExpression> argumentExpressions;
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
        argumentExpressions = FunctionUtil.parseArgumentList (context, ctx.functionCall ().argumentList ());

        /* Validate and find out parameter types. */
        List<Class<?>> parameterTypes = new ArrayList<> ();
        for (ArgumentExpression argument : argumentExpressions) {
            /* Check that no argument is named or has a predicate. */
            if (argument.getName () != null) {
                throw new BrygJitException ("Named arguments are not supported with method calls.", getLine ());
            }

            if (argument.getPredicate () != null) {
                throw new BrygJitException ("Argument predicates are not supported with method calls.", getLine ());
            }

            parameterTypes.add (argument.getType ().getJavaType ());
        }

        /* Find method. */
        Method[] methods = objectType.getMethods ();
        for (Method supposedMethod : methods) {
            if (supposedMethod.getName ().equals (methodName)) {
                if (parameterTypesFit (parameterTypes, supposedMethod.getParameterTypes ())) {
                    method = supposedMethod;
                }
            }
        }

        if (method == null) {
            throw new BrygJitException ("Method " + methodName + " could not be found.", getLine ());
        }

        setType (new Type (method.getReturnType ()));
    }

    private boolean parameterTypesFit (List<Class<?>> searchedTypes, Class<?>[] actualTypes) {
        // TODO: Incorporate coercion and unboxing/boxing in a second run, after the exact
        // matching function has not been found.

        if (searchedTypes.size () != actualTypes.length) return false;

        int i = 0;
        for (Class<?> searchedType : searchedTypes) {
            Class<?> actualType = actualTypes[i];
            if (!actualType.isAssignableFrom (searchedType)) return false;
            ++i;
        }

        return true;
    }

    @Override
    public void compile () {
        Type ownerType = operandExpression.getType ();
        boolean isInterface = ownerType.getJavaType ().isInterface ();

        operandExpression.compile ();
        // -> O

        for (ArgumentExpression argumentExpression : argumentExpressions) {
            argumentExpression.compile ();
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
