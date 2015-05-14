package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

// TODO: This is actually pretty similar to the PackageTree.

/**
 * A tree structure that organizes the package and class names of
 * a list of complete class names in a hierarchical manner.
 */
public class NameIndexTree {

    public enum Kind {
        _class, _package
    }

    private String name;
    private Kind kind;
    private @Nullable Map<String, NameIndexTree> children = null;

    public NameIndexTree(String name) {
        this(name, Kind._package);
    }

    public NameIndexTree(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    private static class Counter {
        int count;
    }

    /**
     * Classes should end with ".class".
     */
    public void addAll(Stream<String> fileNameStream) {
        long start = System.nanoTime();
        final Counter counter = new Counter();
        fileNameStream.forEach(s -> {
            counter.count += 1;
            addName(s);
        });
        System.out.println("Name count: " + counter.count);
        System.out.println("Creating the index took " + ((System.nanoTime() - start) / 1.0e9) + "s.");
    }

    public void addName(String relativeName) {
        String[] nameAndRest = PackageUtil.extractFirstName(relativeName, '/');
        String childName = nameAndRest[0];

        @Nullable NameIndexTree child = getChildren().get(childName);
        if (child == null) {
            Kind childKind;
            if (childName.endsWith(".class")) {
                childName = childName.substring(0, childName.length() - ".class".length());
                childKind = Kind._class;
            } else {
                childKind = Kind._package;
            }

            child = new NameIndexTree(childName, childKind);
            addChild(child);
        }

        if (nameAndRest.length >= 2) {
            child.addName(nameAndRest[1]);
        }
    }

    public void addChild(NameIndexTree index) {
        if (children == null) {
            children = new HashMap<>();
        }
        children.put(index.getName(), index);
    }

    public Map<String, NameIndexTree> getChildren() {
        if (children == null) {
            return Collections.emptyMap();
        } else {
            return children;
        }
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isClass() {
        return kind == Kind._class;
    }

}
