package io.collap.bryg.compiler.resolver;

import java.util.*;

public class ClassResolver {

    private Map<String, String> classNames = new HashMap<> ();

    /** Does not save all classes referenced in classNames. All classes cached in here are lazily loaded. */
    private Map<String, Class<?>> classes = new HashMap<> ();

    private List<Filter> filters = new ArrayList<> ();
    private PackageFilter rootPackageFilter = new PackageFilter (false);

    public ClassResolver () {
        long time = System.nanoTime ();
        filters.add (rootPackageFilter);

        /* java.lang */
        rootPackageFilter.addSubpackageFilter ("java.lang");
        rootPackageFilter.addSubpackageFilter ("java.util");

        resolveClassNames ();

        setResolvedClass ("TestObject", "io.collap.bryg.TestObject");

        System.out.println ("Resolving all classes took " + (System.nanoTime () - time) + "ns.");
    }

    public synchronized Class<?> getResolvedClass (String simpleName) throws ClassNotFoundException {
        String name = classNames.get (simpleName);
        if (name == null) {
            throw new ClassNotFoundException ("Class with the simple name " + simpleName + " not found!");
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

    public void resolveClassNames () {
        ClassNameFinder finder = new ClassNameFinder (new ClassNameVisitor () {
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
        });
        finder.crawl ();
    }

    private boolean isClassImported (String name) {
        for (Filter filter : filters) {
            if (filter.isAccepted (name)) {
                return true;
            }
        }

        return false;
    }

}
