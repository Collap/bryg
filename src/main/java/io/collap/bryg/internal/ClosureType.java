package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.parser.BrygParser;

public class ClosureType extends UnitType {

    private TemplateType parentTemplateType;
    private BrygParser.ClosureBodyContext closureBodyContext;

    private Class<? extends Closure> closureClass;

    public ClosureType(TemplateType parentTemplateType, String className, BrygParser.ClosureBodyContext closureBodyContext) {
        super(className);
        this.parentTemplateType = parentTemplateType;
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

    public void setClosureClass(Class<? extends Closure> closureClass) {
        this.closureClass = closureClass;
    }

    @Override
    public TemplateType getParentTemplateType() {
        return parentTemplateType;
    }

}
