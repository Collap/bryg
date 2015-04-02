package io.collap.bryg.internal.scope;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.VariableInfo;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.Context;

public abstract class CompiledVariable extends VariableInfo {

    public CompiledVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    public abstract void compile(Context context, VariableExpression expression);

}
