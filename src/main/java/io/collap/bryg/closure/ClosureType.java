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

    public ClosureType (TemplateType parentTemplateType, String className, ClosureScope closureScope,
                        BrygParser.ClosureContext closureContext) {
        super (className);
        this.parentTemplateType = parentTemplateType;
        this.closureScope = closureScope;
        this.closureContext = closureContext;
    }

    @Override
    public Class<?> getStandardUnitClass () {
        return StandardClosure.class;
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

    @Override
    public TemplateType getParentTemplateType () {
        return parentTemplateType;
    }

}
