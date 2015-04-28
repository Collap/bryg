package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.Mutability;
import io.collap.bryg.internal.VariableInfo;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.CompiledVariable;
import io.collap.bryg.BrygJitException;

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

    public VariableExpression(CompilationContext compilationContext, int line, CompiledVariable variable,
                              VariableUsageInfo usage) {
        this(compilationContext, line, variable, usage, false);
    }

    public VariableExpression(CompilationContext compilationContext, int line, CompiledVariable variable,
                              VariableUsageInfo usage, boolean forceAssignment) {
        super(compilationContext, line);
        this.usage = usage;
        this.variable = variable;

        setType(variable.getType());
        if (!forceAssignment) {
            checkAccessAndMutability();
        }
    }

    private void checkAccessAndMutability() {
        if (usage.getAccessMode() == AccessMode.set && variable.getMutability() == Mutability.immutable) {
            throw new BrygJitException("The variable '" + variable.getName() + "' is one the left side of an assignment, " +
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
