package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;

// TODO: Find a more fitting name (An "a" tag for example is a "block" function despite belonging to text semantics).

public class HTMLInlineFunction extends HTMLFunction {

    public HTMLInlineFunction (String tag) {
        super (tag);
    }

    public HTMLInlineFunction (String tag, String[] validAttributes) {
        super (tag, validAttributes);
    }

    @Override
    protected void enter (BrygMethodVisitor method, FunctionCallExpression call) {
        method.writeConstantString ("<" + tag);
        new HTMLAttributeCompiler (method, call.getArgumentExpressions (), validAttributes).compile ();
        method.writeConstantString (">");
    }

    @Override
    protected void exit (BrygMethodVisitor method, FunctionCallExpression call) {

    }

}
