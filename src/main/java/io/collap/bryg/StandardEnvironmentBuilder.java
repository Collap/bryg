package io.collap.bryg;

import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.module.Module;

public class StandardEnvironmentBuilder implements EnvironmentBuilder {

    private StandardEnvironment environment;

    public StandardEnvironmentBuilder() {
        environment = new StandardEnvironment();
    }

    @Override
    public void registerModule(Module module) {
        environment.addModule(module);
    }

    @Override
    public void registerSourceLoader(SourceLoader sourceLoader) {
        environment.addSourceLoader(sourceLoader);
    }

    @Override
    public void setClassResolver(ClassResolver classResolver) {
        environment.setClassResolver(classResolver);
    }

    @Override
    public void setDebugConfiguration(DebugConfiguration debugConfiguration) {
        environment.setDebugConfiguration(debugConfiguration);
    }

    @Override
    public Environment build() {
        environment.initialize();
        return environment;
    }

}
