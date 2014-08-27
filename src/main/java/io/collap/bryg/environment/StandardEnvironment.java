package io.collap.bryg.environment;

import io.collap.bryg.Template;
import io.collap.bryg.loader.TemplateClassLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StandardEnvironment implements Environment {

    private Map<String, Template> templateMap = Collections.synchronizedMap (new HashMap<String, Template> ());
    private ClassLoader templateClassLoader;

    public StandardEnvironment (ClassLoader templateClassLoader) {
        this.templateClassLoader = templateClassLoader;
    }

    @Override
    public Template getTemplate (String name) {
        String prefixedName = TemplateClassLoader.templateClassPrefix + name;

        Template template = templateMap.get (prefixedName);

        if (template == null) {
            template = loadTemplate (prefixedName);
        }

        return template;
    }

    /**
     * Also adds the loaded template to the cache.
     */
    private synchronized Template loadTemplate (String prefixedName) {
        Template template = templateMap.get (prefixedName);

        /* There could be a case where getTemplate is called with the same name two or more times,
           which would result in the following scenario:
             1. The first call reaches loadTemplate and calls it. loadTemplate is then locked on this object.
             2. The second call reaches loadTemplate and calls it.
             3. The first call finishes.
             4. The second call can now enter loadTemplate and *loads the template again*.
           The check ensures that the template is not loaded again. */
        if (template == null) {
            try {
                System.out.println ();
                System.out.println ("Template: " + prefixedName);
                long start = System.nanoTime ();
                Class<? extends Template> cl = (Class<? extends Template>) templateClassLoader.loadClass (prefixedName);
                template = cl.newInstance ();
                templateMap.put (prefixedName, template);
                System.out.println ("Loading took " + ((System.nanoTime () - start) / 1.0e9) + "s.");
                System.out.println ();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace ();
            }
        }

        return template;
    }

}
