package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class ClosureType extends UnitType {

    private TemplateType parentTemplateType;
    private BrygParser.ClosureContext closureContext;

    private Class<? extends Closure> closureClass;

    public ClosureType(TemplateType parentTemplateType, String className, BrygParser.ClosureContext closureContext) {
        super(className);
        this.parentTemplateType = parentTemplateType;
        this.closureContext = closureContext;

        addField(new FieldInfo(Types.fromClass(Unit.class), StandardClosure.PARENT_FIELD_NAME, Mutability.immutable,
                Nullness.notnull));
    }

    @Override
    public Class<?> getStandardUnitClass() {
        return StandardClosure.class;
    }

    public BrygParser.ClosureContext getClosureContext() {
        return closureContext;
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
