package io.collap.bryg.loader;

import io.collap.bryg.compiler.Compiler;

public class TemplateClassLoader extends ClassLoader {

    private io.collap.bryg.compiler.Compiler compiler;
    private SourceLoader sourceLoader;

    public TemplateClassLoader (Compiler compiler, SourceLoader sourceLoader) {
        this.compiler = compiler;
        this.sourceLoader = sourceLoader;
    }

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException {
        String source = sourceLoader.getTemplateSource (name);
        byte[] bytecode = compiler.compile (name, source);
        return defineClass (name, bytecode, 0, bytecode.length);
    }

}
