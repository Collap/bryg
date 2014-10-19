package io.collap.bryg.environment;

import io.collap.bryg.StandardTemplate;
import io.collap.bryg.Template;
import io.collap.bryg.TemplateType;
import io.collap.bryg.compiler.Compiler;
import io.collap.bryg.loader.SourceLoader;
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

    private Map<String, TemplateType> templateTypes = Collections.synchronizedMap (new HashMap<String, TemplateType> ());

    private Compiler compiler;
    private SourceLoader sourceLoader;
    private TemplateClassLoader templateClassLoader;
    private Model commonModel;

    public StandardEnvironment (Compiler compiler, SourceLoader sourceLoader, ClassLoader parentClassLoader) {
        this (compiler, sourceLoader, parentClassLoader, new BasicModel ());
    }

    public StandardEnvironment (Compiler compiler, SourceLoader sourceLoader, Model commonModel) {
        this (compiler, sourceLoader, null, commonModel);
    }

    public StandardEnvironment (Compiler compiler, SourceLoader sourceLoader, ClassLoader parentClassLoader, Model commonModel) {
        this.compiler = compiler;
        this.sourceLoader = sourceLoader;
        this.commonModel = commonModel;
        templateClassLoader = new TemplateClassLoader (this, parentClassLoader, compiler);
    }

    @Override
    public @Nullable TemplateType getTemplateType (String name) {
        TemplateType templateType = templateTypes.get (name);

        if (templateType == null) {
            templateType = parseTemplate (name);
        }

        return templateType;
    }

    @Override
    public @Nullable Template getTemplate (String name) {
        TemplateType templateType = getTemplateType (name);

        if (templateType == null) {
            return null;
        }

        if (templateType.getConstructor () == null) {
            boolean success = compileTemplate (templateType);
            if (!success) {
                return null;
            }
        }

        try {
            Constructor<? extends Template> constructor = templateType.getConstructor ();
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

    private synchronized @Nullable TemplateType parseTemplate (String name) {
        TemplateType templateType = templateTypes.get (name);

        /* A similar strategy like in compileTemplate is used here to make sure that templates
           are not parsed twice. */
        if (templateType == null) {
            System.out.println ();
            System.out.println ("Parsing template: " + name);

            String source = sourceLoader.getTemplateSource (name);
            templateType = compiler.parse (name, source);
            if (templateType != null) {
                templateTypes.put (name, templateType);
            }

            System.out.println ();
        }

        return templateType;
    }

    /**
     * Also adds the template constructor to the cache.
     * @return Whether the template was loaded correctly.
     */
    private synchronized boolean compileTemplate (TemplateType templateType) {
        /* There could be a case where getTemplate is called with the same name two or more times,
           which would result in the following scenario:
             1. The first call reaches compileTemplate and calls it. compileTemplate is then locked on this object.
             2. The second call reaches compileTemplate and calls it.
             3. The first call finishes.
             4. The second call can now enter compileTemplate and *compiles the template again*.
           The check ensures that the template is not compiled again. */
        if (templateType.getConstructor () == null) {
            try {
                System.out.println ();
                System.out.println ("Compiling template: " + templateType.getName ());
                // long start = System.nanoTime ();

                Class<? extends Template> cl = (Class<? extends Template>)
                        templateClassLoader.loadClass (TemplateClassLoader.templateClassPrefix + templateType.getName ());
                if (StandardTemplate.class.isAssignableFrom (cl)) {
                    templateType.setConstructor (cl.getConstructor (Environment.class));
                }else {
                    templateType.setConstructor (cl.getConstructor ());
                }

                /* Remove reference to temporary compilation data. */
                templateType.setCompilationData (null);

                // System.out.println ("Loading took " + ((System.nanoTime () - start) / 1.0e9) + "s.");
                System.out.println ();
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                System.out.println ("Constructor could not be loaded: ");
                e.printStackTrace ();
                return false;
            }
        }

        return true;
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
