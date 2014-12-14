package io.collap.bryg.unit;

import io.collap.bryg.compiler.type.RuntimeType;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.template.TemplateType;

import java.util.HashMap;
import java.util.Map;

public abstract class UnitType extends RuntimeType {

    protected String fullName;
    protected String classPackage;
    protected Map<String, FragmentInfo> fragments = new HashMap<> ();

    private String constructorDesc;

    public UnitType (String fullName) {
        super (TypeHelper.toInternalName (fullName));

        this.fullName = fullName;

        /* Set unit package based on name. */
        int lastDot = fullName.lastIndexOf ('.');
        if (lastDot >= 0) {
            classPackage = fullName.substring (0, lastDot);
        }else {
            classPackage = "";
        }
    }

    public String getFullName () {
        return fullName;
    }

    public abstract Class<?> getStandardUnitClass ();

    /**
     * @return The name of the template without the package.
     */
    public String getSimpleName () {
        int index = fullName.lastIndexOf ('.');
        if (index < 0) {
            return fullName;
        }
        return fullName.substring (index + 1);
    }

    public String getClassPackage () {
        return classPackage;
    }

    public void addFragment (FragmentInfo fragment) {
        fragments.put (fragment.getName (), fragment);
    }

    public FragmentInfo getFragment (String name) {
        return fragments.get (name);
    }

    public String getConstructorDesc () {
        return constructorDesc;
    }

    public void setConstructorDesc (String constructorDesc) {
        this.constructorDesc = constructorDesc;
    }

    /**
     * Every UnitType (template or closure) must have at least one corresponding template type, which is itself in the
     * case of templates and the owning template in the case of closures.
     */
    public abstract TemplateType getParentTemplateType ();

}
