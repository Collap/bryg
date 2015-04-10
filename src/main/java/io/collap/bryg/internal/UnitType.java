package io.collap.bryg.internal;

import io.collap.bryg.internal.type.RuntimeType;
import io.collap.bryg.internal.type.TypeHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UnitType extends RuntimeType {

    // TODO: What about the default fragment?

    public static final String DEFAULT_FRAGMENT_NAME = "default";

    protected String fullName;
    protected String classPackage;

    protected List<FieldInfo> fields = new ArrayList<>();
    protected Map<String, FragmentInfo> fragments = new HashMap<>();

    private @Nullable String constructorDesc;

    public UnitType(String fullName) {
        super(TypeHelper.toInternalName(fullName));

        this.fullName = fullName;

        /* Set unit package based on name. */
        int lastDot = fullName.lastIndexOf('.');
        if (lastDot >= 0) {
            classPackage = fullName.substring(0, lastDot);
        } else {
            classPackage = "";
        }
    }

    public void addField(FieldInfo field) {
        fields.add(field);
    }

    public void addFragment(FragmentInfo fragment) {
        fragments.put(fragment.getName(), fragment);
    }

    public FragmentInfo getFragment(String name) {
        return fragments.get(name);
    }

    /**
     * TODO: Comment.
     */
    public abstract Class<?> getStandardUnitClass();

    /**
     * Every UnitType (template or closure) must have at least one corresponding template type, which is itself in the
     * case of templates and the owning template in the case of closures.
     */
    public abstract TemplateType getParentTemplateType();

    public String getFullName() {
        return fullName;
    }

    /**
     * @return The name of the unit without the package.
     */
    public String getSimpleName() {
        int index = fullName.lastIndexOf('.');
        if (index < 0) {
            return fullName;
        }
        return fullName.substring(index + 1);
    }

    public String getClassPackage() {
        return classPackage;
    }

    public String getConstructorDesc() {
        if (constructorDesc == null) {
            throw new IllegalStateException("The constructor description has not been set yet at the time of retrieval.");
        }

        return constructorDesc;
    }

    public void setConstructorDesc(String constructorDesc) {
        this.constructorDesc = constructorDesc;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

}
