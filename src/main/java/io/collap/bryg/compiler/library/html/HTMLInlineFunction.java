package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

public class HTMLInlineFunction extends HTMLFunction {

    public HTMLInlineFunction (String tag, String[] acceptedArguments) {
        super (tag, acceptedArguments);
    }

    @Override
    protected void enter (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("<" + tag);
        new HTMLArgumentCompiler (method, call.getArgumentExpressions (), acceptedArguments).compile ();
        method.writeConstantString ("/>");
    }

    @Override
    protected void exit (BrygMethodVisitor method, FunctionCallExpression call) {

    }

}
