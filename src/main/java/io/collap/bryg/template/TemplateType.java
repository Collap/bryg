package io.collap.bryg.template;

import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.UnitType;

import java.lang.reflect.Constructor;

public class TemplateType extends UnitType {

    /**
     * May be null, in which case the template has not been compiled yet.
     */
    private Constructor<? extends Template> constructor;

    /**
     * This field can be used when the template type has been created, but the
     * template has not been fully compiled. Is set to null after compilation.
     */
    private BrygParser.StartContext startContext;

    public TemplateType (String className, BrygParser.StartContext startContext) {
        super (className);
        this.constructor = null;
        this.startContext = startContext;
    }

    public TemplateType (String className, Constructor<? extends Template> constructor) {
        super (className);
        this.constructor = constructor;
        this.startContext = null;
    }

    public Constructor<? extends Template> getConstructor () {
        return constructor;
    }

    public void setConstructor (Constructor<? extends Template> constructor) {
        this.constructor = constructor;
    }

    public BrygParser.StartContext getStartContext () {
        return startContext;
    }

    public void setStartContext (BrygParser.StartContext startContext) {
        this.startContext = startContext;
    }

}
