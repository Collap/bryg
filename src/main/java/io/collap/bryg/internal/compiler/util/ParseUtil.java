package io.collap.bryg.internal.compiler.util;

public class ParseUtil {

    private static final String[] longSuffixes = new String[] { "l", "L" };
    private static final String[] floatSuffixes = new String[] { "f", "F" };

    public static Long parseLong(String valueString) {
        return Long.parseLong(removeSuffix(valueString, longSuffixes));
    }

    /**
     * Note that, while the parseFloat method accepts the suffixes, for the
     * sake of consistency, we are still removing them before calling the
     * parseFloat method.
     */
    public static Float parseFloat(String valueString) {
        return Float.parseFloat(removeSuffix(valueString, floatSuffixes));
    }

    public static String removeSuffix(String str, String[] suffixes) {
        for (String suffix : suffixes) {
            if (str.endsWith(suffix)) {
                return str.substring(0, str.length() - suffix.length());
            }
        }
        return str;
    }

}
