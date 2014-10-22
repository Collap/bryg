package io.collap.bryg.compiler.util;

import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.UnitClassLoader;

public class IdUtil {

    public static String idToString (BrygParser.IdContext ctx) {
        if (ctx == null) return null;

        String text = ctx.getText ();
        if (text.charAt (0) == '`') { /* This is an escaped ID! */
            text = text.substring (1, text.length () - 1);
        }
        return text;
    }

    /**
     * Prefixes the name if necessary. Assumes that the parentPackage is already prefixed.
     */
    public static String templateIdToString (BrygParser.TemplateIdContext ctx, String parentPackage) {
        if (ctx == null) return null;

        String fullName = ctx.getText ().substring (1); /* Omit the AT (@). */

        /* Check if the parent package needs to be prepended. */
        if (ctx.currentPackage != null) {
            fullName = parentPackage + fullName;
        }else {
            fullName = UnitClassLoader.getPrefixedName (fullName);
        }

        return fullName;
    }

    public static String createGetterName (String fieldName) {
        return "get" + capitalizeFirstLetter (fieldName);
    }

    public static String createSetterName (String fieldName) {
        return "set" + capitalizeFirstLetter (fieldName);
    }

    private static String capitalizeFirstLetter (String str) {
        return str.substring (0, 1).toUpperCase () + str.substring (1);
    }

}
