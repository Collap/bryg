package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.library.Library;

public class HTMLFunctions {

    public static Function html = new HTMLBlockFunction ("html", new String[] { });
    public static Function head = new HTMLBlockFunction ("head", new String[] { });
    public static Function title = new HTMLBlockFunction ("title", new String[] { });

    public static Function body = new HTMLBlockFunction ("body", new String[] { });
    public static Function div = new HTMLBlockFunction ("div", new String[] { });
    public static Function br = new HTMLInlineFunction ("br", new String[] { });
    public static Function img = new HTMLInlineFunction ("img", new String[] { });

    public static void register (Library library) {
        library.setFunction ("html", html);
        library.setFunction ("head", head);
        library.setFunction ("title", title);

        library.setFunction ("body", body);
        library.setFunction ("div", div);
        library.setFunction ("br", br);
        library.setFunction ("img", img);
    }

}
