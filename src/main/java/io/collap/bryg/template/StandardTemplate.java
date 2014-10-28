package io.collap.bryg.template;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.StandardUnit;

public abstract class StandardTemplate extends StandardUnit implements Template {

    protected StandardTemplate (Environment environment) {
        super (environment);
    }

}
