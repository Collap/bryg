package io.collap.bryg.template;

import io.collap.bryg.compiler.Compiler;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.UnitClassLoader;

public class TemplateClassLoader extends UnitClassLoader {

    public TemplateClassLoader (Environment environment, Compiler compiler) {
        super (environment, compiler);
    }

    public TemplateClassLoader (ClassLoader parent, Environment environment, Compiler compiler) {
        super (parent, environment, compiler);
    }

}
