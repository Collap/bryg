package io.collap.bryg.internal;

import io.collap.bryg.Closure;

// TODO: This inherits fields and methods that refer to a constructor, which is not appropriate for an interface type.
// TODO: Templates should also implicitly implement this interface once they specify a default fragment.
public class ClosureInterfaceType extends UnitType {

    protected Class<? extends Closure> interfaceClass;

    public ClosureInterfaceType(String fullName) {
        super(fullName);
        setIsInterface(true);
    }

    @Override
    public Class<?> getStandardUnitClass() {
        return StandardClosure.class;
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (this.similarTo(type)) {
            return true;
        }

        return type instanceof ClosureType && ((ClosureType) type).getInterfaceType().equals(this);
    }

    @Override
    public TemplateType getParentTemplateType() {
        // TODO: This 'null' should, practically, not lead to any complications, but it still games the system. Bad.
        return null;
    }

    public Class<? extends Closure> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<? extends Closure> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

}
