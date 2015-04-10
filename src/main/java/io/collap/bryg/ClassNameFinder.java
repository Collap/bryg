package io.collap.bryg;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// TODO: Cache rt.jar class names in a file, so that the crawling does not take 200ms.
/* TODO/ Test performance. What happens with multiple environments? Possibly cache the
         results somehow. This should not take as much time as it does. */

/*
    Idea to improve performance:
        Build a tree of package names where nodes are optionally marked as end nodes.
        For each jar and directory, the tree is traversed. If a package's name does
        not match a folder, the jar or directory does not contain the requested
        package or any of its children. An end node signals, that all classes defined
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

public class ClassNameFinder {

    private List<String> paths = new ArrayList<>();

    private ClassNameVisitor visitor;

    private List<String> includedJarFiles;

    public ClassNameFinder(ClassNameVisitor visitor, List<String> includedJarFiles) {
        this.visitor = visitor;
        this.includedJarFiles = includedJarFiles;

        /* The *-wildcard signals that all jars in the directory ought to be included in the classpath. */
        addPath(System.getProperty("java.home") + File.separator + "lib" + File.separator + "*");

        /* Add all directories and jars from the classpath. */
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String path : paths) {
            addPath(path);
        }
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public void crawl() {
        crawlPaths(paths);
    }

    public void crawlPaths(List<String> paths) {
        for (String path : paths) {
            crawlPath(path);
        }
    }

    public void crawlPath(String path) {
        if (path.endsWith("*")) { /* Wildcard path. */
            path = path.substring(0, path.length() - 1);
            File directory = new File(path);
            if (!directory.isDirectory()) {
                System.out.println(path + " is not a directory!");
                return;
            }

            /* Crawl jar files. */
            File[] children = directory.listFiles();
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

    public void crawlDirectory(File file) {
        crawlDirectory(file, file);
    }

    private void crawlDirectory(File root, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    crawlDirectory(root, child);
                }
            }
        } else {
            if (file.getName().endsWith(".class")) {
                visitor.visit(createClassName(root, file));
            }
        }
    }

    private String createClassName(File root, File file) {
        Stack<String> names = new Stack<>();
        String name = file.getName();
        names.push(name.substring(0, name.lastIndexOf('.')));

        /* Push names. */
        for (File directory = file.getParentFile();
             directory != null && !directory.equals(root);
             directory = directory.getParentFile()) {
            names.push(directory.getName());
        }

        /* Build the full class name. */
        StringBuilder builder = new StringBuilder(32);
        while (!names.empty()) {
            builder.append(names.pop());
            if (!names.empty()) {
                builder.append('.');
            }
        }

        return builder.toString();
    }

    public void crawlJarFile(File jarFile) {
        if (!includedJarFiles.contains(jarFile.getName())) {
            System.out.println("The JAR " + jarFile.getName() + " is not included in the class name search.");
            return;
        }

        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (Exception ex) {
            System.out.println("Jar file " + jarFile.getAbsolutePath() + " not found!");
            ex.printStackTrace();
            return;
        }

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryPath = entry.getName();
            int extIndex = entryPath.lastIndexOf(".class");
            if (extIndex > 0) {
                visitor.visit(entryPath.substring(0, extIndex).replace("/", "."));
            }
        }
    }

}
