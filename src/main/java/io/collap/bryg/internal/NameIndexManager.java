package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class NameIndexManager {

    private static final NameIndexManager instance = new NameIndexManager();

    public static synchronized NameIndexManager getInstance() {
        return instance;
    }

    /**
     * Saves indices for JAR files at different paths.
     */
    private Map<String, NameIndexTree> indices = new HashMap<>();

    public synchronized @Nullable NameIndexTree getIndex(String path) {
        return indices.get(path);
    }

    /**
     * This method especially should be synchronized to avoid creating multiple indices for the same file.
     */
    public synchronized NameIndexTree createIndex(String path, Stream<String> nameStream) {
        if (indices.containsKey(path)) {
            System.out.println("An index for " + path + " already exists. This is expected behaviour in programs with " +
                    "multiple threads.");
            return indices.get(path);
        }

        // Create a new index.
        NameIndexTree index = new NameIndexTree("");
        index.addAll(nameStream);
        indices.put(path, index);
        return index;
    }

}
