package io.collap.bryg.template;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.Model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TemplateFactory {

    private Environment environment;
    private TemplateType templateType;

    public TemplateFactory (Environment environment, TemplateType templateType) {
        this.environment = environment;
        this.templateType = templateType;
    }

    public Template create (Model model) {
        try {
            Constructor<? extends Template> constructor = templateType.getPublicConstructor ();
            return constructor.newInstance (environment, model);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace ();
            return null;
        }
    }

}
