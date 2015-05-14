package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.parser.BrygParser;

public class ClosureType extends UnitType {

    private TemplateType parentTemplateType;
    private BrygParser.ClosureBodyContext closureBodyContext;

    private Class<? extends Closure> closureClass;
    private ClosureInterfaceType interfaceType;

    public ClosureType(TemplateType parentTemplateType, ClosureInterfaceType interfaceType,
                       String className, BrygParser.ClosureBodyContext closureBodyContext) {
        super(className);
        this.parentTemplateType = parentTemplateType;
        this.interfaceType = interfaceType;
        this.closureBodyContext = closureBodyContext;

        addField(new FieldInfo(parentTemplateType, StandardClosure.PARENT_FIELD_NAME, Mutability.immutable,
                Nullness.notnull));
    }

    @Override
    public Class<?> getStandardUnitClass() {
        return StandardClosure.class;
    }

    public BrygParser.ClosureBodyContext getClosureBodyContext() {
        return closureBodyContext;
    }

    public Class<? extends Closure> getClosureClass() {
        return closureClass;
    }

    public ClosureInterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setClosureClass(Class<? extends Closure> closureClass) {
        this.closureClass = closureClass;
    }

    @Override
    public TemplateType getParentTemplateType() {
        return parentTemplateType;
    }

}
