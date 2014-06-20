package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;

public abstract class BlockFunction implements Function {

    @Override
    public void compile (StandardVisitor visitor, FunctionCallExpression call) {
        BrygMethodVisitor method = visitor.getMethod ();
        enter (method, call);
        call.getStatementOrBlock ().compile ();
        exit (method, call);
    }

    public abstract void enter (BrygMethodVisitor method, FunctionCallExpression call);
    public abstract void exit (BrygMethodVisitor method, FunctionCallExpression call);

}
