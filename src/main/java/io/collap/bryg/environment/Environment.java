package io.collap.bryg.environment;

import io.collap.bryg.Template;
import io.collap.bryg.TemplateType;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;

/**
 * An implementation of Environment <b>must</b> be thread-safe. It must also be guaranteed that any SourceLoader or
 * Compiler instances are used in a thread-safe manner, as these classes are not expected to be thread-safe
 * themselves.
 */
public interface Environment {

    public @Nullable TemplateType getTemplateType (String name);

    /**
     * Returns a <b>new</b> instance of the template each time the method is called.
     * Also loads and compiles the template if it is not already loaded.
     */
    public @Nullable Template getTemplate (String name);

    /**
     * The common model holds variables that are defined in all models created with createModel ().
     */
    public Model getCommonModel ();

    /**
     * Creates a new Model that includes the variables defined in the common model.
     */
    public Model createModel ();

}
