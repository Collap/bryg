package io.collap.bryg.closure;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.StandardUnit;

public abstract class StandardClosure extends StandardUnit implements Closure {

    protected StandardClosure (Environment environment) {
        super (environment);
    }

}
