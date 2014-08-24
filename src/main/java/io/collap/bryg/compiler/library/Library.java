package io.collap.bryg.compiler.library;

public interface Library {

    public Function getFunction (String name);
    public void setFunction (String name, Function function);

}
