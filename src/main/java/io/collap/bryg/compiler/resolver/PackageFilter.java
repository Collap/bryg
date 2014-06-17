package io.collap.bryg.compiler.resolver;

import java.util.HashMap;
import java.util.Map;

public class PackageFilter implements Filter {

    private boolean validEnd; /* Whether the package is a valid last package. */
    private Map<String, PackageFilter> subpackageFilters;

    public PackageFilter (boolean validEnd) {
        this.validEnd = validEnd;
    }

    /**
     * @return The lowest filter in the hierarchy that was created.
     */
    public PackageFilter addSubpackageFilter (String subpackageName) {
        if (subpackageFilters == null) {
            subpackageFilters = new HashMap<> ();
        }

        int firstDot = subpackageName.indexOf ('.');
        String name;
        String rest = null;
        if (firstDot < 0) {
            name = subpackageName;
        }else {
            name = subpackageName.substring (0, firstDot);
            rest = subpackageName.substring (firstDot + 1);
        }

        PackageFilter filter = subpackageFilters.get (name);
        if (filter == null) {
            filter = new PackageFilter (rest == null); /* The filter is only valid at the end of the chain. */
            subpackageFilters.put (name, filter);
        }else if (rest == null) { /* At the end of the chain, but the filter may have existed before without being a valid end. */
            filter.setValidEnd (true);
        }

        if (rest != null) {
            return filter.addSubpackageFilter (rest);
        }else {
            return filter;
        }
    }

    /**
     * @param name Expects the rest of the package string WITHOUT the name of this filter.
     */
    @Override
    public boolean isAccepted (String name) {
        if (subpackageFilters != null) {
            int firstDot = name.indexOf ('.');

            /* At this point it is expected that any call to isAccepted is executed WITH a name that has at least
             * one package prefix, but types without a package are an exception to this guarantee.
             * As types without a package can not pass a package filter in any way, at this point false is returned. */
            if (firstDot < 0) {
                return false;
            }

            String subpackageName = name.substring (0, firstDot);

            PackageFilter filter = subpackageFilters.get (subpackageName);
            if (filter != null) {
                String rest = name.substring (firstDot + 1);
                int restFirstDot = rest.indexOf ('.');
                if (filter.isValidEnd () && restFirstDot < 0) {
                    return true;
                } else {
                    return filter.isAccepted (rest);
                }
            }
        }
        return false;
    }

    public boolean isValidEnd () {
        return validEnd;
    }

    public void setValidEnd (boolean validEnd) {
        this.validEnd = validEnd;
    }

}
