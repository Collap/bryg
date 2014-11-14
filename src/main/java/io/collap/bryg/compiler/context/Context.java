package io.collap.bryg.compiler.context;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.visitor.StandardVisitor;
import io.collap.bryg.compiler.scope.RootScope;
import io.collap.bryg.compiler.scope.Scope;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.Model;
import io.collap.bryg.unit.FragmentInfo;
import io.collap.bryg.unit.UnitType;

import javax.annotation.Nullable;
import java.io.Writer;

public class Context {

    private Environment environment;
    private FragmentInfo fragmentInfo;
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
    public Context (Environment environment, FragmentInfo fragmentInfo, UnitType unitType,
                    @Nullable BrygMethodVisitor methodVisitor, RootScope rootScope) {
        this.environment = environment;
        this.fragmentInfo = fragmentInfo;
        this.unitType = unitType;
        this.parseTreeVisitor = new StandardVisitor ();
        this.methodVisitor = methodVisitor;
        this.rootScope = rootScope;
        currentScope = rootScope;
        closureBlockId = 0;

        /* Set context instance for the parameters. */
        parseTreeVisitor.setContext (this);
        if (methodVisitor != null) methodVisitor.setContext (this);
    }

    /**
     * This name is already prefixed.
     */
    public String getUniqueClosureName () {
        return unitType.getFullName () + "$" + fragmentInfo.getName () + "_Closure" + nextClosureBlockId ();
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

    public FragmentInfo getFragmentInfo () {
        return fragmentInfo;
    }

}
