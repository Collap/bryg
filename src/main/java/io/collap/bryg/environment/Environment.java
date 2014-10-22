package io.collap.bryg.environment;

import io.collap.bryg.compiler.Configuration;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.template.Template;
import io.collap.bryg.template.TemplateType;

import javax.annotation.Nullable;

/**
 * An implementation of Environment <b>must</b> be thread-safe.
 */
public interface Environment {

    public @Nullable TemplateType getTemplateType (String prefixlessName);

    /**
     * This method is intended to be used by bryg internally.
     */
    public @Nullable TemplateType getTemplateTypePrefixed (String name);

    /**
     * Returns a <b>new</b> instance of the template each time the method is called.
     * Also loads and compiles the template if it is not already loaded.
     */
    public @Nullable Template getTemplate (String prefixlessName);

    /**
     * This method is intended to be used by bryg internally.
     */
    public @Nullable Template getTemplatePrefixed (String name);

    public Configuration getConfiguration ();
    public Library getLibrary ();
    public ClassResolver getClassResolver ();

    /**
     * The global variable model holds all global variables declared in the environment.
     */
    public GlobalVariableModel getGlobalVariableModel ();

    public ClassCache getClassCache ();

}
