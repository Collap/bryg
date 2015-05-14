package io.collap.bryg.internal;

import io.collap.bryg.CompilationException;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.RuntimeType;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.util.*;

public abstract class UnitType extends RuntimeType {

    // TODO: This is actually the name of the _delegator to_ the default fragment.
    public static final String DEFAULT_FRAGMENT_NAME = "default";

    protected String fullName;
    protected String classPackage;

    protected List<FieldInfo> fields = new ArrayList<>();
    protected @Nullable ConstructorInfo constructorInfo;
    protected Map<String, FragmentInfo> fragments = new HashMap<>();
    protected @Nullable FragmentInfo defaultFragment = null;

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

    /**
     * This method should only be called once all fields have been set.
     */
    public void configureConstructorInfo() {
        List<ParameterInfo> parameters = new ArrayList<>();
        parameters.add(new ParameterInfo(Types.fromClass(StandardEnvironment.class), StandardUnit.ENVIRONMENT_FIELD_NAME,
                Mutability.immutable, Nullness.notnull, null));
        for (FieldInfo field : fields) {
            parameters.add(new ParameterInfo(field, null)); // TODO: Default values?
        }
        setConstructorInfo(new ConstructorInfo(this, "<init>", parameters));
    }

    public void addField(FieldInfo field) {
        fields.add(field);
    }

    /**
     * Also sets the default fragment field when a default fragment is supplied.
     * @throws CompilationException When two or more default fragments are added to this type.
     */
    public void addFragment(FragmentInfo fragment) {
        fragments.put(fragment.getName(), fragment);
        if (fragment.isDefault()) {
            if (defaultFragment != null) {
                throw new CompilationException("The unit type " + this + " has two (or even more) default fragments: " +
                        "'" + defaultFragment.getName() + "' and '" + fragment.getName() + "'");
            }
            this.defaultFragment = fragment;
        }
    }

    public @Nullable FragmentInfo getFragment(String name) {
        return fragments.get(name);
    }

    public @Nullable FragmentInfo getDefaultFragment() {
        return defaultFragment;
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

    public ConstructorInfo getConstructorInfo() {
        if (constructorInfo == null) {
            throw new IllegalStateException("The constructor info must be set before it can be retrieved.");
        }
        return constructorInfo;
    }

    public void setConstructorInfo(@Nullable ConstructorInfo constructorInfo) {
        this.constructorInfo = constructorInfo;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public Iterator<FragmentInfo> getFragmentIterator() {
        return fragments.values().iterator();
    }

    @Override
    public boolean isUnitType() {
        return true;
    }

}
