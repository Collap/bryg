package io.collap.bryg.module;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.scope.CompiledVariable;

public class MemberVariable extends CompiledVariable implements Member<VariableExpression> {

    public MemberVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    /**
     * This method is both declared in the CompiledVariable class and the Member interface,
     * but the two declarations are contractually equivalent, which means that this override
     * is valid.
     */
    @Override
    public void compile(Context context, VariableExpression node) {
        // TODO: Code.
    }

    @Override
    public Type getResultType() {
        return type;
    }

}
