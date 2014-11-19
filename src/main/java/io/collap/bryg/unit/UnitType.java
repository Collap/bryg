package io.collap.bryg.unit;

import io.collap.bryg.template.TemplateType;

import java.util.HashMap;
import java.util.Map;

public abstract class UnitType {

    protected String fullName;
    protected String classPackage;
    protected Map<String, FragmentInfo> fragments = new HashMap<> ();

    private String jvmName;
    private String descriptor;

    public UnitType (String fullName) {
        this.fullName = fullName;

        /* Set unit package based on name. */
        int lastDot = fullName.lastIndexOf ('.');
        if (lastDot >= 0) {
            classPackage = fullName.substring (0, lastDot);
        }else {
            classPackage = "";
        }
    }

    public String getJvmName () {
        if (jvmName == null) {
            jvmName = fullName.replaceAll ("\\.", "/");
        }
        return jvmName;
    }

    public String getDescriptor () {
        if (descriptor == null) {
            descriptor = "L" + getJvmName () + ";";
        }
        return descriptor;
    }

    public String getFullName () {
        return fullName;
    }

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

    /**
     * Every UnitType (template or closure) must have at least one corresponding template type, which is itself in the
     * case of templates and the owning template in the case of closures.
     */
    public abstract TemplateType getParentTemplateType ();

}
