package io.collap.bryg.unit;

import io.collap.bryg.Unit;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.exception.FragmentCallException;
import io.collap.bryg.model.EmptyModel;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.model.Model;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class StandardUnit implements Unit {

    private static final Model emptyModel = new EmptyModel ();

    /**
     * The name is referenced in {@link io.collap.bryg.compiler.ast.TemplateFragmentCall}.
     */
    protected Environment environment;

    /**
     * The name is referenced in {@link io.collap.bryg.compiler.ast.InDeclarationNode}.
     */
    protected GlobalVariableModel globalVariableModel;

    protected StandardUnit (Environment environment) {
        this.environment = environment;
        this.globalVariableModel = environment.getGlobalVariableModel ();
    }

    @Override
    public void render (Writer writer) throws IOException {
        render (writer, emptyModel);
    }

    @Override
    public void call (String name, Writer writer) throws FragmentCallException {
        call (name, writer, emptyModel);
    }

    @Override
    public void call (String name, Writer writer, Model model) throws FragmentCallException {
        // TODO: Test the performance of this (simple) implementation with reflection.

        Method method;

        try {
            method = getClass ().getMethod (name, Writer.class, Model.class);
        } catch (NoSuchMethodException e) {
            throw new FragmentCallException ("Fragment '" + name + "' could not be found.", e);
        }

        try {
            method.invoke (this, writer, model);
        } catch (IllegalAccessException e) {
            throw new FragmentCallException ("Fragment '" + name + "' could not be accessed.", e);
        } catch (InvocationTargetException e) {
            throw new FragmentCallException ("Call of fragment '" + name + "' did not return successfully.", e);
        }
    }

}
