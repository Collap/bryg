package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class MethodCallExpression extends Expression {

    private Expression operandExpression;
    private List<ArgumentExpression> argumentExpressions = new ArrayList<> ();
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
        List<Class<?>> parameterTypes = new ArrayList<> ();
        List<BrygParser.ArgumentContext> argumentContexts = ctx.functionCall ().argumentList ().argument ();
        for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
            ArgumentExpression argumentExpression = new ArgumentExpression (context, argumentContext);
            argumentExpressions.add (argumentExpression);
            parameterTypes.add (argumentExpression.getType ().getJavaType ());
        }

        /* Find method. */
        try {
            method = objectType.getMethod (methodName, parameterTypes.toArray (new Class<?>[0]));
        } catch (NoSuchMethodException e) {
            e.printStackTrace ();
            throw new BrygJitException ("Method " + methodName + " could not be found.", getLine ());
        }

        setType (new Type (method.getReturnType ()));
    }

    @Override
    public void compile () {
        // TODO: Use invokevirtual for class methods?

        operandExpression.compile ();
        // -> O

        for (ArgumentExpression argumentExpression : argumentExpressions) {
            argumentExpression.compile ();
        }
        // -> A1, A2, ...

        Type ownerType = operandExpression.getType ();
        context.getMethodVisitor ().visitMethodInsn (INVOKEVIRTUAL,
                ownerType.getAsmType ().getInternalName (),
                method.getName (),
                TypeHelper.generateMethodDesc (
                        method.getParameterTypes (),
                        method.getReturnType ()
                ), ownerType.getJavaType ().isInterface ());
        // O, A1, A2, ... -> T
    }

}