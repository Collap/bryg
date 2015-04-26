package io.collap.bryg.internal;

import io.collap.bryg.*;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class StandardUnit implements Unit {

    public static final String ENVIRONMENT_FIELD_NAME = "__environment";

    protected StandardEnvironment __environment;

    protected StandardUnit(StandardEnvironment __environment) {
        this.__environment = __environment;
    }

    @Override
    public void call(String name, Writer writer) throws FragmentCallException {
        call(name, writer, Models.empty());
    }

    @Override
    public void call(String name, Writer writer, Model model) throws FragmentCallException {
        // TODO: Test the performance of this (simple) implementation with reflection.

        Method method;
        try {
            method = getClass().getMethod(name, Writer.class, Model.class);
        } catch (NoSuchMethodException e) {
            throw new FragmentCallException("Fragment '" + name + "' could not be found.", e);
        }

        // TODO: Catch IOException of writer?
        try {
            method.invoke(this, writer, model);
        } catch (IllegalAccessException e) {
            throw new FragmentCallException("Fragment '" + name + "' could not be accessed.", e);
        } catch (InvocationTargetException e) {
            throw new FragmentCallException("Call of fragment '" + name + "' did not return successfully.", e);
        }
    }

}
