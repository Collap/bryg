package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.type.Type;

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
    public final void compile (Context context, FunctionCallExpression call) {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        enter (mv, call);
        call.getStatementOrBlock ().compile ();
        exit (mv, call);
    }

    protected abstract void enter (BrygMethodVisitor method, FunctionCallExpression call);
    protected abstract void exit (BrygMethodVisitor method, FunctionCallExpression call);

    @Override
    public Type getReturnType () {
        return new Type (Void.TYPE);
    }

    public String getTag () {
        return tag;
    }

}