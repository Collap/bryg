package io.collap.bryg.compiler;

import io.collap.bryg.unit.UnitType;

public interface Parser<T extends UnitType> {

    public T parse ();

}
