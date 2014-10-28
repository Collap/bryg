package io.collap.bryg.model;

/**
 * A model that has no variables and can't save any variables.
 */
public class EmptyModel implements Model {

    /**
     * @return Always null.
     */
    @Override
    public Object getVariable (String name) {
        return null;
    }

    @Override
    public void setVariable (String name, Object value) {
        throw new RuntimeException ("The empty model must not be assigned a variable.");
    }

}
