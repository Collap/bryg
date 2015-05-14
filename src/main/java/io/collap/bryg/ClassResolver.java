package io.collap.bryg;

import io.collap.bryg.internal.NameIndexManager;
import io.collap.bryg.internal.NameIndexTree;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ClassResolver {

    private Map<String, String> classNames = new HashMap<>();

    /**
     * Does not save all classes referenced in classNames. All classes cached in here are lazily loaded.
     */
    private Map<String, Class<?>> classes = new HashMap<>();

    private PackageTree rootPackageTree = new PackageTree("");

    private List<String> paths = new ArrayList<>();
    private List<String> includedJarFiles = new ArrayList<>();

    /**
     * This constructor adds package filters for:
     * - java.lang
     * - java.util
     * It includes the jar file <i>rt.jar</i>.
     */
    public ClassResolver() {
        /* java.lang */
        rootPackageTree.addPackage("java.lang");
        rootPackageTree.addPackage("java.util");

        /* Bryg specific language features. */
        setResolvedClass("Closure", Closure.class.getName());

        /* Include Java runtime in the jar files. */
        includeJar("rt.jar");

        /* The *-wildcard signals that all jars in the directory ought to be included in the classpath. */
        addPath(System.getProperty("java.home") + File.separator + "lib" + File.separator + "*");

        /* Add all directories and jars from the classpath. */
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String path : paths) {
            addPath(path);
        }
    }

    public void includeJar(String name) {
        includedJarFiles.add(name);
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public synchronized void resolve() {
        long time = System.nanoTime();
        for (String path : paths) {
            crawlPath(path);
        }
        System.out.println("Resolving all classes took " + ((System.nanoTime() - time) / 1.0e9) + "s.");
    }

    /**
     * Searches for the resolved class referenced by the simple name.
     * If the class is not found and the class resolver has a parent,
     * the getResolvedClass method of that parent is called.
     * Otherwise, a ClassNotFoundException is thrown.
     */
    public synchronized Class<?> getResolvedClass(String simpleName) throws ClassNotFoundException {
        @Nullable String name = classNames.get(simpleName);
        if (name == null) {
            throw new ClassNotFoundException("Class with the simple name " + simpleName + " not found!");
        }

        @Nullable Class<?> cl = classes.get(name);
        if (cl == null) {
            cl = Class.forName(name);
            classes.put(name, cl);
        }

        return cl;
    }

    /**
     * @return Whether the class name was added successfully.
     */
    public synchronized boolean setResolvedClass(String simpleName, String fullName) {
        @Nullable String existingName = classNames.get(simpleName);
        if (existingName != null) {
            if (!existingName.equals(fullName)) {
                System.out.println("Class " + simpleName + " was already registered to " + existingName
                        + ", but it was attempted to register it to " + fullName + "!");
            }
            return false;
        }
        classNames.put(simpleName, fullName);
        return true;
    }

    private void crawlPath(String path) {
        if (path.endsWith("*")) { // Wildcard path.
            path = path.substring(0, path.length() - 1);
            File directory = new File(path);
            if (!directory.isDirectory()) {
                System.out.println(path + " is not a directory!");
                return;
            }

            // Crawl jar files.
            @Nullable File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.getName().endsWith(".jar")) {
                        crawlJarFile(child);
                    }
                }
            }
        } else {
            File file = new File(path);
            if (file.isDirectory()) {
                crawlDirectory(file);
            } else if (file.getName().endsWith(".jar")) {
                crawlJarFile(file);
            }
        }
    }

    private void crawlDirectory(File file) {
        if (file.isDirectory()) {
            crawlDirectory("", rootPackageTree, file);
        } else {
            throw new IllegalArgumentException("The file " + file.getPath() + " is not a directory!");
        }
    }

    private void crawlDirectory(String parentName, PackageTree packageTree, File directory) {
        @Nullable File[] files = directory.listFiles();
        if (files != null) {
            Map<String, PackageTree> childTrees = packageTree.getChildren();
            for (File child : files) {
                if (child.isDirectory()) {
                    @Nullable PackageTree childTree = childTrees.get(child.getName());

                    // Only crawl the directory if it is requested.
                    if (childTree != null) {
                        crawlDirectory(parentName + child.getName() + ".", childTree, child);
                    }
                } else if (child.isFile() && packageTree.shouldInclude()) {
                    importFile(parentName, child);
                }
            }
        }
    }

    private void importFile(String packageName, File file) {
        // Exclude inner classes and anonymous classes.
        String name = file.getName();
        if (name.indexOf('$') >= 0) {
            return;
        }

        if (file.getName().endsWith(".class")) {
            name = name.substring(0, name.lastIndexOf('.'));
            setResolvedClass(name, packageName + name);
        }
    }

    private void crawlJarFile(File jarFile) {
        if (!includedJarFiles.contains(jarFile.getName())) {
            // System.out.println("The JAR " + jarFile.getName() + " is not included in the class name search.");
            return;
        }


        NameIndexManager indexManager = NameIndexManager.getInstance();
        @Nullable NameIndexTree index = indexManager.getIndex(jarFile.getPath());
        if (index == null) {
            JarFile jar;
            try {
                jar = new JarFile(jarFile);
            } catch (Exception ex) {
                throw new RuntimeException("Jar file " + jarFile.getAbsolutePath() + " not found!", ex);
            }

            index = indexManager.createIndex(
                    jarFile.getPath(),
                    jar.stream()
                            .map(ZipEntry::getName) // Get all names.
                            .filter(name -> name.indexOf('$') <= -1) // Exclude inner classes and anonymous classes.
            );
        }

        crawlIndexTree(index, rootPackageTree, "");


        // crawlIndexTree(jar, "", rootPackageTree);


        // TODO: Since we can't extract the directory information from a jar, we can index the jar file and
        //       save the index to a file that persists through JVM executions. The index would also be available
        //       globally, so multiple environments don't spend 100ms - 200ms iterating all entries of the jar.

        /*
            Original idea to improve performance:
                Build a tree of package names where nodes are optionally marked as end nodes.
                For each jar and directory, the tree is traversed. If a package's name does
                not match a folder, the jar or directory does not contain the requested
                package or any of its children. An end node signals that all classes defined
                in the immediate package should be included.
                We could also add class names and wildcards (*) to the tree. A class name would
                mean that a single specific class can be imported. A wildcard would mean that a
                class or package below the wildcard's parent is automatically included.
                This would make filters obsolete, and probably improve performance by quite a bit.
                To make this idea work with jars, we could try to use getJarFile and check for the
                package directories in question.
                This would not have the flexibility of filters, but this will be semantically
                congruent to the import mechanism used in Java, so I doubt we need the flexibility
                that filters potentially provide.
         */

        /* Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryPath = entry.getName();
            int extIndex = entryPath.lastIndexOf(".class");
            if (extIndex > 0) {
                // Exclude inner classes and anonymous classes.
                if (entryPath.indexOf('$') >= 0) {
                    continue;
                }

                String fullName = entryPath.substring(0, extIndex).replace("/", ".");
                int lastDot = fullName.lastIndexOf('.');
                if (lastDot < 0) {
                    System.out.println("Bad file: " + fullName);
                    continue;
                }

                String packageName = fullName.substring(0, lastDot);

                @Nullable PackageTree child = rootPackageTree.findChild(packageName);
                if (child != null && child.shouldInclude()) {
                    String simpleName = fullName.substring(lastDot + 1);
                    System.out.println("Resolve class: " + simpleName);
                    setResolvedClass(simpleName, fullName);
                }
            }
        } */
    }

    private void crawlIndexTree(NameIndexTree index, PackageTree packageTree, String parentPackage) {
        Map<String, NameIndexTree> indexChildren = index.getChildren();

        // Include all classes on the current package level if they should be included.
        if (packageTree.shouldInclude()) {
            indexChildren.values().stream().filter(NameIndexTree::isClass).forEach(
                    c -> setResolvedClass(c.getName(), parentPackage + c.getName())
            );
        }

        // Crawl all applicable subpackages.
        Collection<PackageTree> children = packageTree.getChildren().values();
        for (PackageTree child : children) {
            @Nullable NameIndexTree childIndex = indexChildren.get(child.getName());
            if (childIndex != null) {
                crawlIndexTree(childIndex, child, parentPackage + child.getName() + ".");
            }
        }
    }

    public PackageTree getRootPackageTree() {
        return rootPackageTree;
    }

}
