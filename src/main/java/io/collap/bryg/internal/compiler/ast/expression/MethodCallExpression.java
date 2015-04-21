package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.CompiledType;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

// TODO: This also needs to handle template calls now.

public class MethodCallExpression extends Expression {

    private class CoercionResult {
        public Method method;
        public List<Expression> arguments;

        private CoercionResult(Method method, List<Expression> arguments) {
            this.method = method;
            this.arguments = arguments;
        }
    }

    private Expression operandExpression;
    private CompiledType operandType;
    private List<Expression> argumentExpressions;
    private Method method;

    public MethodCallExpression(CompilationContext compilationContext, BrygParser.MethodCallExpressionContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        operandExpression = (Expression) compilationContext.getParseTreeVisitor().visit(ctx.expression());

        if (!(operandExpression.getType() instanceof CompiledType)) {
            throw new BrygJitException("Can't call a Java method on a non-Java type.", getLine());
        }

        operandType = ((CompiledType) operandExpression.getType());
        if (operandType.getJavaType().isPrimitive()) {
            throw new BrygJitException("Methods can not be invoked on primitives.", getLine());
        }

        /* Init argument expressions. */
        List<ArgumentExpression> arguments = FunctionUtil.parseArgumentList(compilationContext, ctx.functionCall().argumentList());
        argumentExpressions = new ArrayList<>(arguments.size());

        /* Validate arguments. */
        for (ArgumentExpression argument : arguments) {
            /* Check that no argument is named or has a predicate. */
            if (argument.getName() != null) {
                throw new BrygJitException("Named arguments are not supported with method calls.", getLine());
            }

            if (argument.getPredicate() != null) {
                throw new BrygJitException("Argument predicates are not supported with method calls.", getLine());
            }

            argumentExpressions.add(argument);
        }

        /* Find method. */
        findMethod(IdUtil.idToString(ctx.functionCall().id()), operandType.getJavaType());
        setType(Types.fromClass(method.getReturnType()));
    }

    private void findMethod(String methodName, Class<?> objectType) {
        Method[] methods = objectType.getMethods();
        List<CoercionResult> results = findMethods(methodName, methods, false);

        /* If method was not found, try with coercion. */
        if (results.size() == 0) {
            System.out.println("Try with coercion!");
            results = findMethods(methodName, methods, true);
        }

        if (results.size() == 0) {
            throw new BrygJitException("Method " + methodName + " could not be found for the argument types.", getLine());
        } else if (results.size() > 1) {
            // TODO: List coercion results.
            throw new BrygJitException("Coercion of argument types with method " + methodName + " leads to ambiguous results.", getLine());
        }

        CoercionResult result = results.get(0);
        method = result.method;
        if (result.arguments != null) {
            argumentExpressions = result.arguments;
        }
    }

    private List<CoercionResult> findMethods(String methodName, Method[] methods, boolean coerce) {
        List<CoercionResult> results = new ArrayList<>();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                CoercionResult coercionResult = checkMethod(method, coerce);
                if (coercionResult != null) {
                    results.add(coercionResult);
                }
            }
        }
        return results;
    }

    private @Nullable CoercionResult checkMethod(Method method, boolean coerce) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final int numParams = parameterTypes.length;

        if (argumentExpressions.size() != numParams) return null;

        List<Expression> arguments = null;
        if (coerce) {
            arguments = new ArrayList<>(numParams);
        }

        /* Check if the types match. */
        for (int i = 0; i < numParams; ++i) {
            Class<?> paramType = parameterTypes[i];
            Expression expression = argumentExpressions.get(i);
            if (!expression.getType().similarTo(paramType)) { /* Check if the exact types match. */
                if (coerce) {
                    Expression coercionExpression = CoercionUtil.tryUnaryCoercion(compilationContext, expression,
                            Types.fromClass(paramType));
                    if (coercionExpression == null) {
                        return null;
                    }
                    arguments.add(coercionExpression);
                } else {
                    return null;
                }
            } else {
                if (coerce) {
                    arguments.add(expression);
                }
            }
        }

        return new CoercionResult(method, arguments);
    }

    @Override
    public void compile() {
        boolean isInterface = operandType.getJavaType().isInterface();

        operandExpression.compile();
        // -> O

        for (Expression expression : argumentExpressions) {
            expression.compile();
        }
        // -> A1, A2, ...

        compilationContext.getMethodVisitor().visitMethodInsn(isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
                operandType.getInternalName(),
                method.getName(),
                TypeHelper.generateMethodDesc(
                        method.getParameterTypes(),
                        method.getReturnType()
                ), isInterface);
        // O, A1, A2, ... -> T
    }

    @Override
    public void print(PrintStream out, int depth) {
        super.print(out, depth);
        operandExpression.print(out, depth + 1);
    }

}
