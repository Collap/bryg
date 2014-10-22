package io.collap.bryg.closure;

import io.collap.bryg.compiler.Compiler;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.UnitClassLoader;

/**
 * An instance is needed per closure.
 */
public class ClosureClassLoader extends UnitClassLoader {

    public ClosureClassLoader (Environment environment, Compiler compiler) {
        super (environment, compiler);
    }

    public ClosureClassLoader (ClassLoader parent, Environment environment, Compiler compiler) {
        super (parent, environment, compiler);
    }

}
