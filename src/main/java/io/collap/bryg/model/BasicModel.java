package io.collap.bryg.model;

import java.util.HashMap;

public class BasicModel implements Model {

    private Model parent;
    private HashMap<String, Object> variables;

    public BasicModel () {
        this (null);
    }

    public BasicModel (Model parent) {
        this.parent = parent;
        variables = new HashMap<> ();
    }

    @Override
    public Object getVariable (String name) {
        Object value = variables.get (name);
        if (value != null) {
            return value;
        }

        return parent != null ? parent.getVariable (name) : null;
    }

    @Override
    public void setVariable (String name, Object value) {
        variables.put (name, value);
    }

}
