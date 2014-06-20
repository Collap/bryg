package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

public class HTMLBlockFunction extends HTMLFunction {

    public HTMLBlockFunction (String tag, String[] acceptedArguments) {
        super (tag, acceptedArguments);
    }

    @Override
    public void enter (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("<" + tag);
        new HTMLArgumentCompiler (method, call.getArgumentExpressions (), acceptedArguments).compile ();
        method.writeConstantString (">");
    }

    @Override
    public void exit (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("</" + tag + ">");
    }

}
