package io.collap.bryg.compiler.context;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.expression.RootScope;
import io.collap.bryg.compiler.expression.Scope;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.model.Model;

import java.io.Writer;
import java.util.Stack;

public class Context {

    private StandardVisitor parseTreeVisitor;
    private BrygMethodVisitor methodVisitor;
    private Scope rootScope = new RootScope ();
    private Library library;
    private ClassResolver classResolver;

    //
    //  Compiler states
    //

    /**
     * The current scope is the scope each node resides in at its creation.
     */
    private Scope currentScope = rootScope;

    /**
     * When the stack has at least one element, any print output is discarded.
     * This is a Stack instead of a boolean, because 'discard' functions can be nested.
     * The element type has no meaning.
     */
    private Stack<Integer> discardStack = new Stack<> ();

    public Context (BrygMethodVisitor methodVisitor, Library library, ClassResolver classResolver) {
        this.parseTreeVisitor = new StandardVisitor ();
        this.methodVisitor = methodVisitor;
        this.library = library;
        this.classResolver = classResolver;

        /* Register parameters in the correct order. */
        rootScope.registerVariable ("this", null); // TODO: Proper type.
        rootScope.registerVariable ("writer", new Type (Writer.class));
        rootScope.registerVariable ("model", new Type (Model.class));

        /* Set context instance for the parameters. */
        parseTreeVisitor.setContext (this);
        methodVisitor.setContext (this);
    }

    public boolean shouldDiscardPrintOutput () {
        return !discardStack.isEmpty ();
    }

    public void pushDiscard () {
        discardStack.push (0);
    }

    public void popDiscard () {
        discardStack.pop ();
    }

    public StandardVisitor getParseTreeVisitor () {
        return parseTreeVisitor;
    }

    public BrygMethodVisitor getMethodVisitor () {
        return methodVisitor;
    }

    public Scope getRootScope () {
        return rootScope;
    }

    public Library getLibrary () {
        return library;
    }

    public ClassResolver getClassResolver () {
        return classResolver;
    }

    public Scope getCurrentScope () {
        return currentScope;
    }

    public void setCurrentScope (Scope currentScope) {
        this.currentScope = currentScope;
    }

}
