package io.collap.bryg.closure;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.StandardUnit;

public abstract class StandardClosure extends StandardUnit implements Closure {

    // TODO: Find a way to make these fields optional (only compiled if needed), for obvious performance reasons.
    public static final String PARENT_FIELD_NAME = "__parent";

    protected StandardClosure (Environment environment) {
        super (environment);
    }

}
