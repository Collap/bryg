package io.collap.bryg.compiler;

import io.collap.bryg.compiler.loader.TemplateLoader;

public class TemplateClassLoader extends ClassLoader {

    private Compiler compiler;
    private TemplateLoader templateLoader;

    public TemplateClassLoader (Compiler compiler, TemplateLoader templateLoader) {
        this.compiler = compiler;
        this.templateLoader = templateLoader;
    }

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException {
        String source = templateLoader.getTemplateSource (name);
        byte[] bytecode = compiler.compile (name, source);
        System.out.println ("Bytecode size: " + bytecode.length + " Byte");
        return defineClass (name, bytecode, 0, bytecode.length);
    }

}
