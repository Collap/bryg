package io.collap.bryg.environment;

import io.collap.bryg.StandardTemplate;
import io.collap.bryg.Template;
import io.collap.bryg.loader.TemplateClassLoader;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation can only handle templates without constructor parameters or
 * templates that are a subclass of StandardTemplate.
 */
public class StandardEnvironment implements Environment {

    private Model commonModel;
    private Map<String, Constructor<? extends Template>> templateConstructors
            = Collections.synchronizedMap (new HashMap<String, Constructor<? extends Template>> ());
    private TemplateClassLoader templateClassLoader;

    public StandardEnvironment (TemplateClassLoader templateClassLoader) {
        this (templateClassLoader, new BasicModel ());
    }

    public StandardEnvironment (TemplateClassLoader templateClassLoader, Model commonModel) {
        this.templateClassLoader = templateClassLoader;
        this.commonModel = commonModel;
    }

    @Override
    public Template getTemplate (String name) {
        String prefixedName = TemplateClassLoader.templateClassPrefix + name;

        Constructor<? extends Template> constructor = templateConstructors.get (prefixedName);

        if (constructor == null) {
            constructor = loadTemplate (prefixedName);
            if (constructor == null) { /* May still be null! */
                return null;
            }
        }

        try {
            if (constructor.getParameterTypes ().length <= 0) {
                return constructor.newInstance ();
            }else { /* StandardTemplate. */
                return constructor.newInstance (this);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace ();
            return null;
        }
    }

    /**
     * Also adds the template constructor to the cache.
     * @return The constructor of the template, or null if it could not be loaded.
     */
    private synchronized @Nullable Constructor<? extends Template> loadTemplate (String prefixedName) {
        Constructor<? extends Template> constructor = templateConstructors.get (prefixedName);

        /* There could be a case where getTemplate is called with the same name two or more times,
           which would result in the following scenario:
             1. The first call reaches loadTemplate and calls it. loadTemplate is then locked on this object.
             2. The second call reaches loadTemplate and calls it.
             3. The first call finishes.
             4. The second call can now enter loadTemplate and *loads the template again*.
           The check ensures that the template is not loaded again. */
        if (constructor == null) {
            try {
                System.out.println ();
                System.out.println ("Template: " + prefixedName);
                long start = System.nanoTime ();
                Class<? extends Template> cl = (Class<? extends Template>) templateClassLoader.loadClass (prefixedName);
                if (StandardTemplate.class.isAssignableFrom (cl)) {
                    constructor = cl.getConstructor (Environment.class);
                }else {
                    constructor = cl.getConstructor ();
                }
                templateConstructors.put (prefixedName, constructor);
                System.out.println ("Loading took " + ((System.nanoTime () - start) / 1.0e9) + "s.");
                System.out.println ();
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                System.out.println ("Constructor could not be loaded: ");
                e.printStackTrace ();
                return null;
            }
        }

        return constructor;
    }

    @Override
    public Model getCommonModel () {
        return commonModel;
    }

    @Override
    public Model createModel () {
        return new BasicModel (commonModel);
    }

}
