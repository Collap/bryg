package io.collap.bryg;

import io.collap.bryg.internal.PackageUtil;

import javax.annotation.Nullable;
import java.util.*;

public class PackageTree {

    private String name;

    /**
     * Whether the current node should include/import the class names of immediate child classes.
     */
    private boolean shouldInclude = false;
    private @Nullable Map<String, PackageTree> children = null;

    public PackageTree(String name) {
        this.name = name;
    }

    public void addPackage(String relativePackage) {
        String[] nameAndRest = PackageUtil.extractFirstName(relativePackage, '.');
        String childName = nameAndRest[0];

        @Nullable PackageTree child = getChildren().get(childName);
        if (child == null) {
            child = new PackageTree(childName);
            addChild(child);
        }

        if (nameAndRest.length < 2) {
            // Every end-point should include the classes defined in the current package.
            child.setShouldInclude(true);
        } else {
            child.addPackage(nameAndRest[1]);
        }
    }

    public @Nullable PackageTree findChild(String relativePackage) {
        String[] nameAndRest = PackageUtil.extractFirstName(relativePackage, '.');
        @Nullable PackageTree child = getChildren().get(nameAndRest[0]);
        if (child != null) {
            if (nameAndRest.length >= 2) {
                return child.findChild(nameAndRest[1]);
            } else {
                return child; // Last child in chain.
            }
        } else {
            return null;
        }
    }

    private void addChild(PackageTree child) {
        if (children == null) {
            children = new HashMap<>();
        }
        children.put(child.getName(), child);
    }

    public Map<String, PackageTree> getChildren() {
        if (children == null) {
            return Collections.emptyMap();
        } else {
            return children;
        }
    }

    public String getName() {
        return name;
    }

    public boolean shouldInclude() {
        return shouldInclude;
    }

    public void setShouldInclude(boolean shouldInclude) {
        this.shouldInclude = shouldInclude;
    }

}
