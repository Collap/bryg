package io.collap.bryg.internal;

import io.collap.bryg.Model;
import io.collap.bryg.Template;
import io.collap.bryg.TemplateFactory;
import io.collap.bryg.TemplateInstantiationException;
import io.collap.bryg.template.TemplateType;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StandardTemplateFactory implements TemplateFactory {

    private StandardEnvironment environment;
    private TemplateType templateType;

    public StandardTemplateFactory(StandardEnvironment environment, TemplateType templateType) {
        this.environment = environment;
        this.templateType = templateType;
    }

    @Override
    public @Nonnull Template instantiate(Model model) {
        try {
            Constructor<? extends Template> constructor = templateType.getPublicConstructor();
            return constructor.newInstance(environment, model);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            // Since this case should NOT happen under normal circumstances, we can throw
            // an unchecked exception that notifies the user directly.
            throw new TemplateInstantiationException(templateType.getFullName(), e);
        }
    }

}
