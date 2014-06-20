package io.collap.bryg.environment;

import io.collap.bryg.Template;
import io.collap.bryg.compiler.StandardCompiler;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.loader.SourceLoader;
import io.collap.bryg.loader.TemplateClassLoader;

import java.util.HashMap;
import java.util.Map;

public class StandardEnvironment implements Environment {

    private Map<String, Template> templateMap = new HashMap<> ();
    private ClassLoader templateClassLoader;

    public StandardEnvironment (SourceLoader sourceLoader) {
        this (new TemplateClassLoader (new StandardCompiler (new ClassResolver ()), sourceLoader));
    }

    public StandardEnvironment (ClassLoader templateClassLoader) {
        this.templateClassLoader = templateClassLoader;
    }

    @Override
    public Template getTemplate (String name) {
        Template template = templateMap.get (name);

        if (template == null) {
            template = loadTemplate (name);
        }

        return template;
    }

    /**
     * Also adds the loaded template to the cache.
     */
    private synchronized Template loadTemplate (String name) {
        Template template = null;
        try {
            Class<? extends Template> cl = (Class<? extends Template>) templateClassLoader.loadClass (name);
            template = cl.newInstance ();
            templateMap.put (name, template);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace ();
        }

        return template;
    }

}
