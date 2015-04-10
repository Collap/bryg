package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.Mutability;
import io.collap.bryg.internal.VariableInfo;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.CompiledVariable;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

/**
 * This Node <b>may</b> be instantiated in other Node's compile methods, since
 * no scopes are accessed by this node's constructors.
 *
 * // TODO: The coupling of this VariableExpression node and CompiledVariables is bad.
 *          Specifically, to compile a variable expression "on the fly", an instance
 *          of this class needs to be created, which is then immediately thrown away.
 */
public class VariableExpression extends Expression {

    private CompiledVariable variable;
    private VariableUsageInfo usage;

    public VariableExpression(CompilationContext compilationContext, BrygParser.VariableExpressionContext ctx,
                              VariableUsageInfo usage) {
        super(compilationContext);
        setLine(ctx.getStart().getLine());
        this.usage = usage;

        String variableName = IdUtil.idToString(ctx.variable().id());
        @Nullable CompiledVariable variable = compilationContext.getCurrentScope().getVariable(variableName);
        if (variable == null) {
            throw new BrygJitException("Variable " + variableName + " not found!", getLine());
        }else {
            this.variable = variable;
        }

        setType(variable.getType());
        checkAccessAndMutability();
    }

    public VariableExpression(CompilationContext compilationContext, int line, CompiledVariable variable,
                              VariableUsageInfo usage) {
        super(compilationContext);
        setLine(line);
        this.usage = usage;
        this.variable = variable;

        setType(variable.getType());
        checkAccessAndMutability();
    }

    private void checkAccessAndMutability() {
        if (usage.getAccessMode() == AccessMode.set && variable.getMutability() == Mutability.immutable) {
            throw new BrygJitException("The variable " + variable.getName() + " is one the left side of an assignment, " +
                    "but it is immutable.", getLine());
        }
    }

    @Override
    public void compile() {
        variable.compile(compilationContext, usage);
    }

    public VariableInfo getVariable() {
        return variable;
    }

}
