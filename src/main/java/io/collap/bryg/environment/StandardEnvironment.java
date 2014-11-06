package io.collap.bryg.environment;

import io.collap.bryg.compiler.Configuration;
import io.collap.bryg.compiler.TemplateCompiler;
import io.collap.bryg.compiler.TemplateParser;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.template.Template;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.loader.SourceLoader;
import io.collap.bryg.template.TemplateClassLoader;
import io.collap.bryg.unit.UnitClassLoader;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * This implementation can only handle templates without constructor parameters or
 * templates that are a subclass of StandardTemplate.
 */
public class StandardEnvironment implements Environment {

    private final Map<String, TemplateType> templateTypes = Collections.synchronizedMap (new HashMap<String, TemplateType> ());
    private final HashMap<String, Future<Boolean>> currentCompilations = new HashMap<> ();

    private Configuration configuration;
    private Library library;
    private ClassResolver classResolver;
    private SourceLoader sourceLoader;
    private GlobalVariableModel globalVariableModel;
    private ClassCache classCache;

    public StandardEnvironment (Configuration configuration, Library library, ClassResolver classResolver,
                                SourceLoader sourceLoader) {
        this (configuration, library, classResolver, sourceLoader, new GlobalVariableModel ());
    }

    public StandardEnvironment (Configuration configuration, Library library, ClassResolver classResolver,
                                SourceLoader sourceLoader, GlobalVariableModel globalVariableModel) {
        this.configuration = configuration;
        this.library = library;
        this.classResolver = classResolver;
        this.sourceLoader = sourceLoader;
        this.globalVariableModel = globalVariableModel;
        this.classCache = new ClassCache ();
    }

    @Override
    public @Nullable TemplateType getTemplateType (String prefixlessName) {
        return getTemplateTypePrefixed (UnitClassLoader.getPrefixedName (prefixlessName));
    }

    @Override
    public @Nullable TemplateType getTemplateTypePrefixed (String name) {
        TemplateType templateType = templateTypes.get (name);

        if (templateType == null) {
            templateType = parseTemplate (name);
        }

        return templateType;
    }


    @Override
    public @Nullable Template getTemplate (String prefixlessName) {
        return getTemplatePrefixed (UnitClassLoader.getPrefixedName (prefixlessName));
    }

    @Override
    public @Nullable Template getTemplatePrefixed (String name) {
        TemplateType templateType = getTemplateTypePrefixed (name);

        if (templateType == null) {
            return null;
        }

        if (!templateType.isCompiled ()) {
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
            TemplateParser parser = new TemplateParser (this, name, source);

            templateType = parser.parse ();
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
        if (!templateType.isCompiled ()) {
            try {
                String className = templateType.getFullName ();

                System.out.println ();
                System.out.println ("Compiling template: " + className);
                // long start = System.nanoTime ();

                TemplateCompiler compiler = new TemplateCompiler (this, templateType);
                TemplateClassLoader templateClassLoader = new TemplateClassLoader (this, compiler);

                Class<? extends Template> cl = (Class<? extends Template>) templateClassLoader.loadClass (className);
                templateType.setTemplateClass (cl);
                /* Cache class. */
                classCache.cacheClass (className, cl);

                /* Compile all referenced templates. */
                for (TemplateType referencedTemplate : templateType.getReferencedTemplates ()) {
                    compileTemplate (referencedTemplate);
                }

                /* Remove references to temporary compilation data. */
                templateType.clearCompilationData ();

                // System.out.println ("Loading took " + ((System.nanoTime () - start) / 1.0e9) + "s.");
                System.out.println ();
            } catch (ClassNotFoundException e) {
                System.out.println ("Template " + templateType.getFullName () + " could not be loaded: ");
                e.printStackTrace ();
                return false;
            }
        }

        return true;
    }

    @Override
    public Configuration getConfiguration () {
        return configuration;
    }

    @Override
    public Library getLibrary () {
        return library;
    }

    @Override
    public ClassResolver getClassResolver () {
        return classResolver;
    }

    @Override
    public GlobalVariableModel getGlobalVariableModel () {
        return globalVariableModel;
    }

    @Override
    public ClassCache getClassCache () {
        return classCache;
    }

}
