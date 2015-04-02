package io.collap.bryg.internal;

import io.collap.bryg.Template;
import io.collap.bryg.Environment;

public abstract class StandardTemplate extends StandardUnit implements Template {

    protected StandardTemplate (Environment environment) {
        super (environment);
    }

}
