package io.collap.bryg.compiler.expression;

public class ClassType implements Type {

    public static final ClassType OBJECT = new ClassType (Object.class);
    public static final ClassType STRING = new ClassType (String.class);
    public static final ClassType INTEGER = new ClassType (Integer.class);

    public static boolean isString (Type type) {
        return type.equals (STRING);
    }

    private Class<?> actualType;
    private String jvmName = null;
    private String descriptorFormatString = null;

    public ClassType (Class<?> actualType) {
        this.actualType = actualType;
    }

    private void generateJvmName () {
        String className = actualType.getName ();
        jvmName = className.replace ('.', '/');
    }

    private void generateDescriptorFormatString () {
        descriptorFormatString = "L" + getJvmName () + ";";
    }

    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof ClassType)) {
            return false;
        }

        ClassType classType = (ClassType) obj;
        return classType.getActualType ().equals (actualType);
    }

    @Override
    public Class<?> getActualType () {
        return actualType;
    }

    public String getJvmName () {
        if (jvmName == null) {
            generateJvmName ();
        }
        return jvmName;
    }

    @Override
    public String toDescriptorFormat () {
        if (descriptorFormatString == null) {
            generateDescriptorFormatString ();
        }
        return descriptorFormatString;
    }
}
