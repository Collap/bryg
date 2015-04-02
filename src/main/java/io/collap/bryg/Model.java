package io.collap.bryg;

import javax.annotation.Nullable;

public interface Model {

    /* TODO/  Wrap the Object inside a "perhaps undefined" wrapper, to distinguish between "null" and "undefined".
              In the latter case, the default argument has to be assigned. */

    /**
     * @return The value associated with the variable or null if the variable was not found.
     */
    public @Nullable Object getVariable(String name);

    /**
     * @param value The class of 'value' must be a class that is imported to the ClassResolver.
     */
    public void setVariable(String name, Object value);

}
