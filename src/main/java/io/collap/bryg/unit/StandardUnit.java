package io.collap.bryg.unit;

import io.collap.bryg.Unit;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.EmptyModel;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.model.Model;

import java.io.IOException;
import java.io.Writer;

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
    public void call (String name, Writer writer) throws IOException, InvalidInputParameterException, ClassCastException {
        call (name, writer, emptyModel);
    }

}
