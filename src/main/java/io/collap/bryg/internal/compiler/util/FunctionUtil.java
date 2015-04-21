package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.BrygJitException;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.util.*;

public class FunctionUtil {

    public static List<ParameterInfo> parseParameterList(StandardEnvironment environment,
                                               @Nullable List<BrygParser.ParameterDeclarationContext> contexts) {
        List<ParameterInfo> parameters = new ArrayList<>();
        if (contexts != null) {
            for (BrygParser.ParameterDeclarationContext context : contexts) {
                String name = IdUtil.idToString(context.id());
                Type type = new TypeInterpreter(environment.getClassResolver()).interpretType(context.type());

                // TODO: Handle default values.
                Nullness nullness = context.nullable.getType() == BrygLexer.NULLABLE ? Nullness.nullable : Nullness.notnull;
                parameters.add(new ParameterInfo(type, name, Mutability.immutable, nullness, null));
            }
        }
        return parameters;
    }

    public static List<ArgumentExpression> parseArgumentList(CompilationContext compilationContext,
                                                             @Nullable BrygParser.ArgumentListContext ctx) {
        List<ArgumentExpression> expressions = new ArrayList<>();
        if (ctx != null) {
            List<BrygParser.ArgumentContext> argumentContexts = ctx.argument();
            for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
                expressions.add(new ArgumentExpression(compilationContext, argumentContext));
            }
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
                    throw new BrygJitException("No parameter exists for named argument " + argument.getName(), line);
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
