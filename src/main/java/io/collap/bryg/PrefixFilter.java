package io.collap.bryg;

/**
 * This Filter accepts all class names that start with a specific prefix.
 * It can be used to import a package and its subpackages without further semantics.
 */
public class PrefixFilter implements Filter {

    private String prefix;

    public PrefixFilter (String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean isAccepted (String name) {
        return name.startsWith (prefix);
    }

}
