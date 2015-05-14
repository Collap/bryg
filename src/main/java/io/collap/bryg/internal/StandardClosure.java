package io.collap.bryg.internal;

import io.collap.bryg.Closure;

public abstract class StandardClosure extends StandardUnit implements Closure {

    public static final String PARENT_FIELD_NAME = "parent";

    protected StandardClosure(StandardEnvironment environment) {
        super(environment);
    }

}
