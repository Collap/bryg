package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Field;

// TODO: Rename to NameUtil?

public class IdUtil {

    public static String idToString(BrygParser.IdContext ctx) {
        String text = ctx.getText();
        if (text.charAt(0) == '`') { /* This is an escaped ID! */
            text = text.substring(1, text.length() - 1);
        }
        return text;
    }

    public static String templateIdToString(CompilationContext compilationContext, BrygParser.TemplateIdContext ctx) {
        String name = ctx.getText().substring(1); // Omit the AT (@).
        if (ctx.currentPackage != null) {
            return compilationContext.getUnitType().getClassPackage() + name;
        } else {
            return name;
        }
    }

    public static String createGetterName(Field field) {
        String name = field.getName();

        if (field.getType().equals(Boolean.TYPE)) {
            if (nameHasIsPrefix(name)) {
                return name;
            } else {
                return "is" + capitalizeFirstLetter(name);
            }
        }

        return "get" + capitalizeFirstLetter(name);
    }

    public static String createSetterName(Field field) {
        String name = field.getName();

        if (field.getType().equals(Boolean.TYPE)) {
            if (nameHasIsPrefix(name)) {
                name = name.substring("is".length());
            }
            return "set" + capitalizeFirstLetter(name);
        }

        return "set" + capitalizeFirstLetter(name);
    }

    private static String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * A particular field name has an "is" prefix, if the first two characters are "is",
     * immediately followed by an upper case letter. For example: isVisible, isAlive, isRich
     */
    private static boolean nameHasIsPrefix(String name) {
        return name.length() >= 3 && name.startsWith("is") && Character.isUpperCase(name.charAt(2));
    }

}
