package io.collap.bryg.closure;

import io.collap.bryg.compiler.scope.ClosureScope;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.unit.UnitType;

public class ClosureType extends UnitType {

    private TemplateType parentTemplateType;
    private ClosureScope closureScope;
    private BrygParser.ClosureContext closureContext;

    private Class<? extends Closure> closureClass;
    private String constructorDesc;

    public ClosureType (TemplateType parentTemplateType, String className, ClosureScope closureScope,
                        BrygParser.ClosureContext closureContext) {
        super (className);
        this.parentTemplateType = parentTemplateType;
        this.closureScope = closureScope;
        this.closureContext = closureContext;
    }

    public ClosureScope getClosureScope () {
        return closureScope;
    }

    public BrygParser.ClosureContext getClosureContext () {
        return closureContext;
    }

    public Class<? extends Closure> getClosureClass () {
        return closureClass;
    }

    public void setClosureClass (Class<? extends Closure> closureClass) {
        this.closureClass = closureClass;
    }

    public String getConstructorDesc () {
        return constructorDesc;
    }

    public void setConstructorDesc (String constructorDesc) {
        this.constructorDesc = constructorDesc;
    }

    @Override
    public TemplateType getParentTemplateType () {
        return parentTemplateType;
    }

}
