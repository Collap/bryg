package io.collap.bryg.compiler;

import io.collap.bryg.unit.UnitType;

/**
 * A compiler must be created for one unit and then discarded.
 */
public interface Compiler<T extends UnitType> {

    public byte[] compile ();

    public T getUnitType ();

}
