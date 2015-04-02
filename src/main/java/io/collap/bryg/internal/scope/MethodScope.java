package io.collap.bryg.internal.scope;

import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.VariableInfo;
import io.collap.bryg.internal.compiler.VariableIdCounter;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.UnitType;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.List;
import java.util.Set;

// TODO: Should be named FragmentScope.

public class MethodScope extends LocalScope {

    public MethodScope(UnitScope parent, VariableIdCounter variableIdCounter,
                       UnitType unitType, @Nullable List<ParameterInfo> parameters) {
        super(parent, variableIdCounter);

        registerLocalVariable(new LocalVariable(unitType, "this", false, false));
        registerLocalVariable(new LocalVariable(Types.fromClass(Writer.class), "writer", false, false));

        if (parameters != null) {
            for (VariableInfo parameter : parameters) {
                registerLocalVariable(new LocalVariable(parameter.getType(), parameter.getName(), false));
            }
        }
    }

    @Override
    public @Nullable Variable getVariable(String name) {
        System.out.println("Method Scope: " + name);

        Variable variable = super.getVariable(name); /* Also prefers fields over global variables. */
        if (variable == null) {
            GlobalVariableModel.GlobalVariable globalVariable = globalVariableModel.getDeclaredVariable(name);
            if (globalVariable != null) {
                Type type = Types.fromClass(globalVariable.getJavaType());
                LocalVariable localVariable = new LocalVariable(type, name, false);
                localVariable.setId(calculateNextId(type));
                variables.put(name, localVariable);
                globalVariablesUsed.add(name);
                variable = localVariable;
            }
        }

        return variable;
    }

    @Override
    public void registerVariableInternal(Variable variable) {
        if (globalVariablesUsed.contains(variable.getName())) {
            throw new BrygJitException("Global variable " + variable.getName() + " is overshadowed by a variable of " +
                    "the same name in the method scope, but the global varible has already been used.", -1);
        }

        super.registerVariableInternal(variable);
    }

    public GlobalVariableModel getGlobalVariableModel() {
        return globalVariableModel;
    }

    public Set<String> getGlobalVariablesUsed() {
        return globalVariablesUsed;
    }

}
