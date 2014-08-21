package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;

public class HTMLBlockFunction extends HTMLFunction {

    public HTMLBlockFunction (String tag) {
        super (tag);
    }

    public HTMLBlockFunction (String tag, String[] validAttributes) {
        super (tag, validAttributes);
    }

    @Override
    public void enter (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("<" + tag);
        new HTMLAttributeCompiler (method, call.getArgumentExpressions (), validAttributes).compile ();
        method.writeConstantString (">");
    }

    @Override
    public void exit (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("</" + tag + ">");
    }

}
