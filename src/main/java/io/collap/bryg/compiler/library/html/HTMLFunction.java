package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;

public abstract class HTMLFunction implements Function {

    protected String tag;
    protected String[] validAttributes;

    protected HTMLFunction (String tag) {
        this (tag, new String[] { });
    }

    /**
     * @param validAttributes These lists <b>must</b> be sorted alphabetically (A to Z).
     */
    protected HTMLFunction (String tag, String[] validAttributes) {
        this.tag = tag;
        this.validAttributes = validAttributes;
    }

    @Override
    public final void compile (StandardVisitor visitor, FunctionCallExpression call) {
        BrygMethodVisitor method = visitor.getMethod ();
        enter (method, call);
        call.getStatementOrBlock ().compile ();
        exit (method, call);
    }

    protected abstract void enter (BrygMethodVisitor method, FunctionCallExpression call);
    protected abstract void exit (BrygMethodVisitor method, FunctionCallExpression call);

    @Override
    public Class<?> getReturnType () {
        return Void.TYPE;
    }

    public String getTag () {
        return tag;
    }

}