package io.collap.bryg.compiler.helper;

import io.collap.bryg.parser.BrygParser;

public class IdHelper {

    public static String idToString (BrygParser.IdContext ctx) {
        if (ctx == null) return null;

        String text = ctx.getText ();
        if (text.charAt (0) == '`') { /* This is an escaped ID! */
            text = text.substring (1, text.length () - 1);
        }
        return text;
    }

}
