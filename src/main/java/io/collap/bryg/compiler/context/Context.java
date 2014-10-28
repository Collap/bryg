package io.collap.bryg.compiler.context;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.visitor.StandardVisitor;
import io.collap.bryg.compiler.scope.RootScope;
import io.collap.bryg.compiler.scope.Scope;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.Model;
import io.collap.bryg.unit.UnitType;

import javax.annotation.Nullable;
import java.io.Writer;

public class Context {

    private Environment environment;
    private UnitType unitType;
    private StandardVisitor parseTreeVisitor;
    private BrygMethodVisitor methodVisitor;
    private RootScope rootScope;
    private int closureBlockId;

    //
    //  Compiler states
    //

    /**
     * The current scope is the scope each node resides in at its creation.
     */
    private Scope currentScope;

    /**
     * If the counter is greater than 1, any print output is discarded.
     * This is a counter instead of a boolean, because 'discard' functions can be nested.
     */
    private int discardsOpen = 0;

    /**
     * @param methodVisitor May be null, but should be set before compiling any nodes.
     */
    public Context (Environment environment, UnitType unitType,
                    @Nullable BrygMethodVisitor methodVisitor, RootScope rootScope) {
        this.environment = environment;
        this.unitType = unitType;
        this.parseTreeVisitor = new StandardVisitor ();
        this.methodVisitor = methodVisitor;
        this.rootScope = rootScope;
        currentScope = rootScope;
        closureBlockId = 0;

        /* Register parameters in the correct order. */

        /* Unless we change the way how types are handled by the ‚compiler, we can not assign a proper type here. */
        rootScope.registerVariable (new Variable (new Type (Object.class), "this", false));

        rootScope.registerVariable (new Variable (new Type (Writer.class), "writer", false));
        rootScope.registerVariable (new Variable (new Type (Model.class), "model", false));

        /* Set context instance for the parameters. */
        parseTreeVisitor.setContext (this);
        if (methodVisitor != null) methodVisitor.setContext (this);
    }

    /**
     * This name is already prefixed.
     */
    public String getUniqueClosureName () {
        return unitType.getFullName () + "$Closure" + nextClosureBlockId ();
    }

    private int nextClosureBlockId () {
        int id = closureBlockId;
        ++closureBlockId;
        return id;
    }

    public boolean shouldDiscardPrintOutput () {
        return discardsOpen > 0;
    }

    public void pushDiscard () {
        ++discardsOpen;
    }

    public void popDiscard () {
        --discardsOpen;
    }

    public Environment getEnvironment () {
        return environment;
    }

    public UnitType getUnitType () {
        return unitType;
    }

    public StandardVisitor getParseTreeVisitor () {
        return parseTreeVisitor;
    }

    public void setMethodVisitor (BrygMethodVisitor methodVisitor) {
        methodVisitor.setContext (this);
        this.methodVisitor = methodVisitor;
    }

    public BrygMethodVisitor getMethodVisitor () {
        return methodVisitor;
    }

    public RootScope getRootScope () {
        return rootScope;
    }

    public Scope getCurrentScope () {
        return currentScope;
    }

    public void setCurrentScope (Scope currentScope) {
        this.currentScope = currentScope;
    }

}
