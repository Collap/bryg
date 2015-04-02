package io.collap.bryg.internal.compiler;

import io.collap.bryg.internal.UnitType;

public interface Parser<T extends UnitType> {

    public T parse ();

}
