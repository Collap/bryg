package io.collap.bryg.compiler.util;

import io.collap.bryg.parser.BrygParser;

public class IdUtil {

    public static String idToString (BrygParser.IdContext ctx) {
        if (ctx == null) return null;

        String text = ctx.getText ();
        if (text.charAt (0) == '`') { /* This is an escaped ID! */
            text = text.substring (1, text.length () - 1);
        }
        return text;
    }

    public static String templateIdToString (BrygParser.TemplateIdContext ctx) {
        if (ctx == null) return null;

        return ctx.getText ().substring (1); /* Omit the AT (@). */
    }

}
