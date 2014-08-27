package io.collap.bryg.loader;

import io.collap.bryg.compiler.Compiler;

public class TemplateClassLoader extends ClassLoader {

    /**
     * This prefix is used to distinguish between actual templates and other classes.
     * It should minimize naming conflicts.
     */
    public static final String templateClassPrefix = "template.";

    private io.collap.bryg.compiler.Compiler compiler;
    private SourceLoader sourceLoader;

    public TemplateClassLoader (Compiler compiler, SourceLoader sourceLoader) {
        this (TemplateClassLoader.class.getClassLoader (), compiler, sourceLoader);
    }

    public TemplateClassLoader (ClassLoader parent, Compiler compiler, SourceLoader sourceLoader) {
        super (parent);
        this.compiler = compiler;
        this.sourceLoader = sourceLoader;
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

        String source = sourceLoader.getTemplateSource (name);
        byte[] bytecode = compiler.compile (name, source);
        return defineClass (name, bytecode, 0, bytecode.length);
    }

}
