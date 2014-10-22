package io.collap.bryg.unit;

import io.collap.bryg.Unit;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.GlobalVariableModel;

import java.io.Writer;

public abstract class StandardUnit implements Unit {

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
    public void render (Writer writer) {
        render (writer, null);
    }

}
