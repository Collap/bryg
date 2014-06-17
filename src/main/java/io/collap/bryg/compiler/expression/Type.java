package io.collap.bryg.compiler.expression;

public interface Type {

    public String toDescriptorFormat ();
    public Class<?> getActualType ();

}
