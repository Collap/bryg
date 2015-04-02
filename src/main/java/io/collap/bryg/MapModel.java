package io.collap.bryg;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MapModel implements Model {

    private @Nullable Model parent;
    private Map<String, Object> variables;

    public MapModel() {
        this(null);
    }

    public MapModel(@Nullable Model parent) {
        this(parent, new HashMap<String, Object>());
    }

    public MapModel(@Nullable Model parent, Map<String, Object> variables) {
        this.parent = parent;
        this.variables = variables;
    }

    @Override
    public @Nullable Object getVariable(String name) {
        @Nullable Object value = variables.get(name);
        if (value != null) {
            return value;
        } else {
            if (parent != null) {
                return parent.getVariable(name);
            } else {
                return null;
            }
        }
    }

    @Override
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

}
