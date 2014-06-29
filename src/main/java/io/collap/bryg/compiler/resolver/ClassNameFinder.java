package io.collap.bryg.compiler.resolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassNameFinder {

    private List<String> paths = new ArrayList<> ();

    private ClassNameVisitor visitor;

    public ClassNameFinder (ClassNameVisitor visitor) {
        this.visitor = visitor;

        // TODO: Include the executed jar file.

        /* The *-wildcard signals that all jars in the directory ought to be included in the classpath. */
        addPath (System.getProperty ("java.home") + File.separator + "lib" + File.separator + "*");

        /* Add all directories and jars from the classpath. */
        String[] paths = System.getProperty ("java.class.path").split (System.getProperty ("path.separator"));
        for (String path : paths) {
            addPath (path);
        }
    }

    public void addPath (String path) {
        paths.add (path);
    }

    public void crawl () {
        crawlPaths (paths);
    }

    public void crawlPaths (List<String> paths) {
        for (String path : paths) {
            crawlPath (path);
        }
    }

    public void crawlPath (String path) {
        if (path.endsWith ("*")) { /* Wildcard path. */
            path = path.substring (0, path.length () - 1);
            File directory = new File (path);
            if (!directory.isDirectory ()) {
                System.out.println (path + " is not a directory!");
                return;
            }

            /* Crawl jar files. */
            File[] children = directory.listFiles ();
            for (File child : children) {
                if (child.getName ().endsWith (".jar")) {
                    crawlJarFile (child);
                }
            }
        }else {
            File file = new File (path);
            if (file.isDirectory ()) {
                crawlDirectory (file);
            }else if (file.getName ().endsWith (".jar")) {
                crawlJarFile (file);
            }
        }
    }

    public void crawlDirectory (File file) {
        crawlDirectory (file, file);
    }

    private void crawlDirectory (File root, File file) {
        if (file.isDirectory ()) {
            for (File child : file.listFiles ()) {
                crawlDirectory (root, child);
            }
        }else {
            if (file.getName ().endsWith (".class")) {
                visitor.visit (createClassName (root, file));
            }
        }
    }

    private String createClassName (File root, File file) {
        Stack<String> names = new Stack<> ();
        String name = file.getName ();
        names.push (name.substring (0, name.lastIndexOf ('.')));

        /* Push names. */
        for (File directory = file.getParentFile ();
                directory != null && !directory.equals (root);
                directory = directory.getParentFile ()) {
            names.push (file.getName ());
        }

        /* Build the full class name. */
        StringBuilder builder = new StringBuilder (32);
        while (!names.empty ()) {
            builder.append (names.pop ());
            if (!names.empty ()) {
                builder.append ('.');
            }
        }

        return builder.toString();
    }

    public void crawlJarFile (File jarFile) {
        JarFile jar;
        try {
            jar = new JarFile (jarFile);
        } catch (Exception ex) {
            System.out.println ("Jar file " + jarFile.getAbsolutePath () + " not found!");
            ex.printStackTrace ();
            return;
        }

        Enumeration<JarEntry> entries = jar.entries ();
        while (entries.hasMoreElements ()) {
            JarEntry entry = entries.nextElement ();
            String entryPath = entry.getName ();
            int extIndex = entryPath.lastIndexOf (".class");
            if (extIndex > 0) {
                visitor.visit (entryPath.substring (0, extIndex).replace ("/", "."));
            }
        }
    }

}
