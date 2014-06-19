package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;

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
        public Class<?> getReturnType () {
            return Void.TYPE;
        }
    }

    public static class HTMLInlineFunction implements Function {

        private String tag;

        public HTMLInlineFunction (String tag) {
            this.tag = tag;
        }

        @Override
        public void compile (StandardVisitor visitor, FunctionCallExpression call) {
            visitor.getMethod ().writeConstantString ("<" + tag + "/>");
        }

        @Override
        public Class<?> getReturnType () {
            return Void.TYPE;
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
