package io.collap.bryg.model;

public interface Model {

    /**
     * @return The value associated with the variable or null if the variable was not found.
     */
    public Object getVariable (String name);

    /**
     * @param value The class of 'value' must be a class that is imported to the ClassResolver.
     */
    public void setVariable (String name, Object value);

}
