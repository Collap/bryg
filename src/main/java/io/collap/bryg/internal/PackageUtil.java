package io.collap.bryg.internal;

public class PackageUtil {

    /**
     * @return { 1. Element: The name until the first delimiter, or relativePackage;
     *           2. Element: If first delimiter is found, the rest of the package string, otherwise nonexistent }
     */
    public static String[] extractFirstName(String relativePackage, char pathDelimiter) {
        int firstDelimiter = relativePackage.indexOf(pathDelimiter);
        if (firstDelimiter < 0) {
            return new String[] { relativePackage };
        }else {
            return new String[] {
                    relativePackage.substring (0, firstDelimiter),
                    relativePackage.substring (firstDelimiter + 1)
            };
        }
    }

}
