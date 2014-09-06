package io.collap.bryg.compiler.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassResolver {

    /**
     * The parent class resolver allows to, for example, have multiple class resolvers that
     * scan different packages, but have one parent that handles the rt.jar classes.
     */
    private ClassResolver parent;

    private Map<String, String> classNames = new HashMap<> ();

    /** Does not save all classes referenced in classNames. All classes cached in here are lazily loaded. */
    private Map<String, Class<?>> classes = new HashMap<> ();

    private PackageFilter rootPackageFilter = new PackageFilter (false);
    private List<Filter> filters = new ArrayList<Filter> () {{ add (rootPackageFilter); }};
    private List<String> includedJarFiles = new ArrayList<> ();

    private ClassNameFinder finder = new ClassNameFinder (new ClassNameVisitor () {
        @Override
        public void visit (String fullName) {
                /* Exclude inner classes and anonymous classes. */
            if (fullName.indexOf ('$') >= 0) {
                return;
            }

            if (isClassImported (fullName)) {
                int lastDot = fullName.lastIndexOf ('.');
                String simpleName = fullName.substring (lastDot + 1);
                setResolvedClass (simpleName, fullName);
            }
        }
    }, includedJarFiles);

    /**
     * This constructor adds package filters for:
     *   - java.lang
     *   - java.util
     * And includes the jar file <i>rt.jar</i>.
     */
    public ClassResolver () {
        /* java.lang */
        rootPackageFilter.addSubpackageFilter ("java.lang");
        rootPackageFilter.addSubpackageFilter ("java.util");

        /* Include Java runtime in the jar files. */
        includedJarFiles.add ("rt.jar");
    }

    /**
     * This constructor does not add any filters, neither does it include jar files.
     */
    public ClassResolver (ClassResolver parent) {
        this.parent = parent;
    }

    /**
     * Searches for the resolved class referenced by the simple name.
     * If the class is not found and the class resolver has a parent,
     * the getResolvedClass method of that parent is called.
     * Otherwise, a ClassNotFoundException is thrown.
     */
    public synchronized Class<?> getResolvedClass (String simpleName) throws ClassNotFoundException {
        String name = classNames.get (simpleName);
        if (name == null) {
            if (parent != null) {
                return parent.getResolvedClass (simpleName);
            }else {
                throw new ClassNotFoundException ("Class with the simple name " + simpleName + " not found!");
            }
        }

        Class<?> cl = classes.get (name);
        if (cl == null) {
            cl = Class.forName (name);
            classes.put (name, cl);
        }

        return cl;
    }

    /**
     * @return Whether the class name was added successfully.
     */
    public synchronized boolean setResolvedClass (String simpleName, String fullName) {
        String existingName = classNames.get (simpleName);
        if (existingName != null) {
            if (!existingName.equals (fullName)) {
                System.out.println ("Class " + simpleName + " was already registered to " + existingName
                        + ", but attempted to register to " + fullName + "!");
            }
            return false;
        }
        classNames.put (simpleName, fullName);
        return true;
    }

    public void addFilter (Filter filter) {
        filters.add (filter);
    }

    public void includeJar (String name) {
        includedJarFiles.add (name);
    }

    public void resolveClassNames () {
        long time = System.nanoTime ();
        finder.crawl ();
        System.out.println ("Resolving all classes took " + ((System.nanoTime () - time) / 1.0e9) + "s.");
    }

    private boolean isClassImported (String name) {
        for (Filter filter : filters) {
            if (filter.isAccepted (name)) {
                return true;
            }
        }

        return false;
    }

    public ClassResolver getParent () {
        return parent;
    }

    public List<Filter> getFilters () {
        return filters;
    }

    public PackageFilter getRootPackageFilter () {
        return rootPackageFilter;
    }

    public ClassNameFinder getFinder () {
        return finder;
    }

}
