package io.collap.bryg.internal.compiler;

import io.collap.bryg.internal.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The state of the compiler for a single function.
 */
public class CompilationContext {

    private StandardEnvironment environment;
    private FunctionInfo functionInfo;
    private UnitType unitType;
    private StandardVisitor parseTreeVisitor;
    private @Nullable BrygMethodVisitor methodVisitor;
    private FunctionScope functionScope;
    private UnitScope unitScope;
    private int closureBlockId;

    //
    //  Compiler states
    //

    /**
     * The current scope is the scope each node resides in at its creation.
     * This is a local scope, because this is at least the function scope and at most any local sub-scope.
     */
    private LocalScope currentScope;

    /**
     * Modules are allowed to register fragment-wide states in this map.
     */
    private Map<String, Object> states = new HashMap<>();


    private int discardsOpen = 0;

    /**
     * @param methodVisitor May be null, but should be set before compiling any nodes.
     */
    public CompilationContext(StandardEnvironment environment, FunctionInfo functionInfo, UnitType unitType,
                              @Nullable BrygMethodVisitor methodVisitor, FunctionScope functionScope,
                              UnitScope unitScope) {
        // TODO: Change the order of the parameters to: environment, unitType, unitScope, functionInfo, functionScope, methodVisitor
        this.environment = environment;
        this.functionInfo = functionInfo;
        this.unitType = unitType;
        this.parseTreeVisitor = new StandardVisitor();
        this.methodVisitor = methodVisitor;
        this.functionScope = functionScope;
        this.unitScope = unitScope;
        currentScope = functionScope;
        closureBlockId = 0;

        /* Set context instance for the parameters. */
        parseTreeVisitor.setCompilationContext(this);
        if (methodVisitor != null) {
            methodVisitor.setCompilationContext(this);
        }
    }

    public String getUniqueClosureName() {
        return unitType.getFullName() + "$" + functionInfo.getName() + "_Closure" + nextClosureBlockId();
    }

    private int nextClosureBlockId() {
        int id = closureBlockId;
        ++closureBlockId;
        return id;
    }

    public boolean shouldDiscardPrintOutput() {
        return discardsOpen > 0;
    }

    public void pushDiscard() {
        ++discardsOpen;
    }

    public void popDiscard() {
        --discardsOpen;
    }

    public StandardEnvironment getEnvironment() {
        return environment;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public StandardVisitor getParseTreeVisitor() {
        return parseTreeVisitor;
    }

    public void setMethodVisitor(BrygMethodVisitor methodVisitor) {
        methodVisitor.setCompilationContext(this);
        this.methodVisitor = methodVisitor;
    }

    public BrygMethodVisitor getMethodVisitor() {
        if (methodVisitor == null) {
            throw new IllegalStateException("The method visitor must not be null once it is retrieved from the context.");
        }

        return methodVisitor;
    }

    public FunctionScope getFunctionScope() {
        return functionScope;
    }

    public UnitScope getUnitScope() {
        return unitScope;
    }

    public LocalScope getCurrentScope() {
        return currentScope;
    }

    public void setCurrentScope(LocalScope currentScope) {
        this.currentScope = currentScope;
    }

    public FunctionInfo getFunctionInfo() {
        return functionInfo;
    }

    public @Nullable Object getState(String key) {
        return states.get(key);
    }

    public Object getStateOrDefault(String key, Object defaultValue) {
        return states.putIfAbsent(key, defaultValue);
    }

    public void setState(String key, Object value) {
        states.put(key, value);
    }

}
