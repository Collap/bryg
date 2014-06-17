package io.collap.bryg.model;

import java.util.HashMap;

public class BasicModel implements Model {

    private HashMap<String, Object> variables;

    public BasicModel () {
        variables = new HashMap<> ();
    }

    @Override
    public Object getVariable (String name) {
        return variables.get (name);
    }

    @Override
    public void setVariable (String name, Object value) {
        variables.put (name, value);
    }

}
