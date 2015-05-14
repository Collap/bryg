package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.BrygJitException;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.FragmentInfo;
import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.ast.expression.ClosureInstantiationExpression;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.util.*;

public class FunctionUtil {

    public static List<ParameterInfo> parseParameterList(StandardEnvironment environment,
                                               @Nullable List<BrygParser.ParameterDeclarationContext> contexts) {
        List<ParameterInfo> parameters = new ArrayList<>();
        if (contexts != null) {
            boolean hadImplicitParameter = false;
            for (BrygParser.ParameterDeclarationContext context : contexts) {
                String name = IdUtil.idToString(context.id());

                boolean isImplicit = context.implicit != null;
                if (isImplicit) {
                    if (hadImplicitParameter) {
                        throw new BrygJitException("There are two or more implicit parameters. There can only be one!" +
                                " The offender is called '" + name + "'",
                                context.getStart().getLine());
                    } else {
                        hadImplicitParameter = true;
                    }
                }

                Type type = new TypeInterpreter(environment).interpretType(context.type());

                // TODO: Handle default values.
                Nullness nullness = context.nullable != null ? Nullness.nullable : Nullness.notnull;
                parameters.add(new ParameterInfo(type, name, Mutability.immutable, nullness, null, isImplicit));
            }
        }
        return parameters;
    }

    public static List<ArgumentExpression> parseArgumentList(CompilationContext compilationContext,
                                                             @Nullable BrygParser.ArgumentListContext ctx) {
        return parseArgumentList(compilationContext, ctx, null, null);
    }

    /**
     * @param closureCtx May be null, in which case 'fragmentInfo' is unused, but may be either null or not null.
     * @param fragmentInfo Must not be null when 'closureCtx' is not null.
     */
    public static List<ArgumentExpression> parseArgumentList(CompilationContext compilationContext,
                                                             @Nullable BrygParser.ArgumentListContext ctx,
                                                             @Nullable BrygParser.ClosureContext closureCtx,
                                                             @Nullable FragmentInfo fragmentInfo) {
        List<ArgumentExpression> expressions = new ArrayList<>();

        if (ctx != null) {
            List<BrygParser.ArgumentContext> argumentContexts = ctx.argument();
            for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
                expressions.add(new ArgumentExpression(compilationContext, argumentContext));
            }
        }

        if (closureCtx != null) {
            if (fragmentInfo == null) {
                throw new IllegalArgumentException("The fragment info must be not null when the closure context is supplied." +
                        " This is a compiler bug!");
            }

            @Nullable ParameterInfo implicitParameter = fragmentInfo.getImplicitParameter();
            if (implicitParameter == null) {
                throw new BrygJitException("Attempted to instantiate an implicit closure, but there is no" +
                        " corresponding parameter. (Did you forget to declare a Closure parameter as 'implicit'?)", closureCtx.getStart().getLine());
            }

            // This is added as a named argument, so that the argument reordering algorithm picks the right slot.
            expressions.add(new ArgumentExpression(
                    compilationContext, closureCtx.getStart().getLine(),
                    new ClosureInstantiationExpression(compilationContext, closureCtx),
                    implicitParameter.getName(), null
            ));
        }

        return expressions;
    }

    /**
     * Orders the arguments in the supplied list based on named arguments.
     * Named arguments are assigned to their corresponding parameters first,
     * then the other unnamed arguments are assigned to the remaining parameters
     * from left to right, without performing any type checks or coercion checks.
     * // TODO: Return list of parameters that did not have an argument.
     */
    public static List<ArgumentExpression> reorderArgumentList(CompilationContext compilationContext, int line,
                                                               List<ArgumentExpression> arguments,
                                                               List<ParameterInfo> parameters) {
        List<ArgumentExpression> orderedArguments = new ArrayList<>(Collections.nCopies(arguments.size(), null));

        // Tracks the parameters that have not been
        List<Integer> unusedParameterIndices = new ArrayList<>();
        for (int i = 0; i < parameters.size(); ++i) {
            unusedParameterIndices.add(i);
        }

        // "Eliminate" all named arguments first.
        for (ArgumentExpression argument : arguments) {
            if (argument.getName() != null) {
                // Find corresponding parameter.
                ListIterator<Integer> it = unusedParameterIndices.listIterator();
                @Nullable ParameterInfo correspondingParameter = null;
                int index = -1;
                while (it.hasNext()) {
                    index = it.next();
                    ParameterInfo parameter = parameters.get(index);
                    if (parameter.getName().equals(argument.getName())) {
                        correspondingParameter = parameter;
                        it.remove();
                        break;
                    }
                }

                if (correspondingParameter == null) {
                    throw new BrygJitException("No parameter exists for named argument " + argument.getName() + " or " +
                            " there are two or more arguments with the same name.", line);
                }

                orderedArguments.set(index, argument);
            }
        }

        // Then assign the rest of the arguments to the first parameter available.
        for (ArgumentExpression argument : arguments) {
            if (argument.getName() == null) {
                if (!unusedParameterIndices.isEmpty()) {
                    // Note: Overload is tricky here. This is removing the FIRST element.
                    int index = unusedParameterIndices.remove(0);
                    orderedArguments.set(index, argument);
                }else {
                    throw new BrygJitException("Not enough parameters for the amount of arguments.", line);
                }
            }
        }

        return orderedArguments;
    }

}
