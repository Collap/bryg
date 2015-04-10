package io.collap.bryg.internal;

import io.collap.bryg.Template;

public abstract class StandardTemplate extends StandardUnit implements Template {

    protected StandardTemplate(StandardEnvironment environment) {
        super(environment);
    }

}
