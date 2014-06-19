package io.collap.bryg.environment;

import io.collap.bryg.Template;
import io.collap.bryg.compiler.StandardCompiler;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.loader.SourceLoader;
import io.collap.bryg.loader.TemplateClassLoader;

import java.util.HashMap;
import java.util.Map;

public class StandardEnvironment implements Environment {

    private io.collap.bryg.compiler.Compiler compiler;
    private Map<String, Template> templateMap = new HashMap<> ();
    private Map<String, TemplateClassLoader> classLoaderMap = new HashMap<> (); // prefix -> SourceLoader

    public StandardEnvironment () {
        compiler = new StandardCompiler (new ClassResolver ());
    }

    public void registerSourceLoader (String prefix, SourceLoader sourceLoader) {
        classLoaderMap.put (prefix, new TemplateClassLoader (compiler, sourceLoader));
    }

    @Override
    public Template getTemplate (String prefix, String name) {
        String fullName = prefix + "." + name;
        Template template = templateMap.get (fullName);

        if (template == null) {
            TemplateClassLoader classLoader = classLoaderMap.get (prefix);
            if (classLoader != null) {
                try {
                    Class<? extends Template> cl = (Class<? extends Template>) classLoader.loadClass (name);
                    template = cl.newInstance ();
                    templateMap.put (fullName, template);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace ();
                }
            }
        }

        return template;
    }

}
