package io.collap.bryg.loader;

import io.collap.bryg.TemplateType;
import io.collap.bryg.compiler.Compiler;
import io.collap.bryg.environment.Environment;

public class TemplateClassLoader extends ClassLoader {

    /**
     * This prefix is used to distinguish between actual templates and other classes.
     * It should minimize naming conflicts.
     */
    public static final String templateClassPrefix = "template.";

    private Environment environment;
    private io.collap.bryg.compiler.Compiler compiler;


    public TemplateClassLoader (Environment environment, Compiler compiler) {
        this (environment, TemplateClassLoader.class.getClassLoader (), compiler);
    }

    public TemplateClassLoader (Environment environment, ClassLoader parent, Compiler compiler) {
        super (parent);
        this.environment = environment;
        this.compiler = compiler;
    }

    @Override
    protected Class<?> findClass (String prefixedName) throws ClassNotFoundException {
        if (!prefixedName.startsWith (templateClassPrefix)) {
            /* Try to load with default class loader. */
            System.out.println ("TemplateClassLoader was requested to load '" + prefixedName + "', which is not a template name. " +
                    "Attempting to use system class loader.");
            return Class.forName (prefixedName);
        }

        /* Remove prefix. */
        String name = prefixedName.substring (templateClassPrefix.length ());

        TemplateType templateType = environment.getTemplateType (name);
        if (templateType == null) {
            throw new ClassNotFoundException ("Template type for '" + name + "' could not be found.");
        }

        byte[] bytecode = compiler.compile (templateType);
        return defineClass (name, bytecode, 0, bytecode.length);
    }

}
