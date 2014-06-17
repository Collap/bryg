package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;

public abstract class BlockFunction implements Function {

    @Override
    public void compile (RenderVisitor visitor, FunctionCallExpression call) {
        BrygMethodVisitor method = visitor.getMethod ();
        enter (method);
        call.getStatementOrBlock ().compile ();
        exit (method);
    }

    public abstract void enter (BrygMethodVisitor method);
    public abstract void exit (BrygMethodVisitor method);

}
