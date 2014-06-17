package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.compiler.expression.Type;

public class HTMLFunctions {

    public static class HTMLBlockFunction extends BlockFunction {

        private String tag;

        public HTMLBlockFunction (String tag) {
            this.tag = tag;
        }

        @Override
        public void enter (BrygMethodVisitor method) {
            method.writeConstantString ("<" + tag + ">");
        }

        @Override
        public void exit (BrygMethodVisitor method) {
            method.writeConstantString ("</" + tag + ">");
        }

        @Override
        public Type getReturnType () {
            return PrimitiveType._void;
        }
    }

    public static class HTMLInlineFunction implements Function {

        private String tag;

        public HTMLInlineFunction (String tag) {
            this.tag = tag;
        }

        @Override
        public void compile (RenderVisitor visitor, FunctionCallExpression call) {
            visitor.getMethod ().writeConstantString ("<" + tag + "/>");
        }

        @Override
        public Type getReturnType () {
            return PrimitiveType._void;
        }
    }

    public static Function html = new HTMLBlockFunction ("html");
    public static Function head = new HTMLBlockFunction ("head");
    public static Function title = new HTMLBlockFunction ("title");

    public static Function body = new HTMLBlockFunction ("body");
    public static Function br = new HTMLInlineFunction ("br");

    public static void register (Library library) {
        library.setFunction ("html", html);
        library.setFunction ("head", head);
        library.setFunction ("title", title);

        library.setFunction ("body", body);
        library.setFunction ("br", br);
    }

}
