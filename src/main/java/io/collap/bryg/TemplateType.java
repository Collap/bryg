package io.collap.bryg;

import java.lang.reflect.Constructor;

public class TemplateType {

    private String name;
    private TemplateInfo info;

    /**
     * May be null, in which case the template has not been compiled yet.
     */
    private Constructor<? extends Template> constructor;

    /**
     * This field can be used when the template type has been created, but the
     * template has not been fully compiled.
     */
    private CompilationData compilationData;

    public TemplateType (String name, TemplateInfo info, CompilationData compilationData) {
        this.name = name;
        this.info = info;
        this.constructor = null;
        this.compilationData = compilationData;
    }

    public TemplateType (String name, TemplateInfo info, Constructor<? extends Template> constructor) {
        this.name = name;
        this.info = info;
        this.constructor = constructor;
        this.compilationData = null;
    }

    public String getName () {
        return name;
    }

    public Constructor<? extends Template> getConstructor () {
        return constructor;
    }

    public TemplateInfo getInfo () {
        return info;
    }

    public CompilationData getCompilationData () {
        return compilationData;
    }

    public void setConstructor (Constructor<? extends Template> constructor) {
        this.constructor = constructor;
    }

    public void setCompilationData (CompilationData compilationData) {
        this.compilationData = compilationData;
    }

}
