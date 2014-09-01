package io.collap.bryg;

import io.collap.bryg.environment.Environment;

/**
 * This particular implementation is used by the StandardCompiler.
 */
public abstract class StandardTemplate implements Template {

    /**
     * The name is referenced in ast.TemplateFragmentCall.
     */
    protected Environment environment;

    protected StandardTemplate (Environment environment) {
        this.environment = environment;
    }

}
