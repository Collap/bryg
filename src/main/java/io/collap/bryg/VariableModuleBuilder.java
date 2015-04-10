package io.collap.bryg;

import io.collap.bryg.internal.type.Types;
import io.collap.bryg.module.GenericModule;
import io.collap.bryg.module.MemberVariable;
import io.collap.bryg.module.Module;

import javax.annotation.Nullable;

public class VariableModuleBuilder {

    private GenericModule module;

    public VariableModuleBuilder(String name, Visibility visibility) {
        module = new GenericModule(name, visibility);
    }

    /**
     * Registers a variable that may not be null.
     */
    public <T> void registerVariable(String name, Class<T> type, T value) {
        registerVariable(name, type, value, Nullness.notnull);
    }

    public <T> void registerVariable(String name, Class<T> type, @Nullable T value, Nullness nullness) {
        if (nullness == Nullness.notnull && value == null) {

        }

        module.setMember(name, new MemberVariable<>(Types.fromClass(type), name, value, nullness));
    }

    public Module build() {
        return module;
    }



}
