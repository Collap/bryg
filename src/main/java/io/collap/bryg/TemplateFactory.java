package io.collap.bryg;

import javax.annotation.Nonnull;

/**
 * Using the factory is like writing "new category.Overview(model)", but since we can't
 * access the template class at compile time, since it is only compiled at runtime,
 * you need to use it like this:
 *
 * <pre>{@code
 *      TemplateFactory factory = environment.getTemplateFactory("category.Overview");
 *      Template template = factory.instantiate(fields);
 *      template.call("default", writer, Models.empty());
 * }</pre>
 */
public interface TemplateFactory {

    /**
     * @throws io.collap.bryg.TemplateInstantiationException When the template could not be instantiated. This should
     *         not happen under normal circumstances. The cause is attached to the exception, as well as the name of
     *         the template in question.
     */
    public @Nonnull Template instantiate(Model model);

}
