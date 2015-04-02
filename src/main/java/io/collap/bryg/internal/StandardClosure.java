package io.collap.bryg.internal;

import io.collap.bryg.Closure;
import io.collap.bryg.Environment;

public abstract class StandardClosure extends StandardUnit implements Closure {

    // TODO: Find a way to make these fields optional (only compiled if needed), for obvious performance reasons.
    public static final String PARENT_FIELD_NAME = "__parent";

    protected StandardClosure(Environment environment) {
        super(environment);
    }

}
