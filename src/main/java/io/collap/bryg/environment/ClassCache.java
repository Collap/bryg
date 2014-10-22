package io.collap.bryg.environment;

import io.collap.bryg.Unit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class caches all unit classes that are compiled by bryg.
 * It is necessary, because especially closure classes, who are
 * statically loaded by the class creating the closure for setting
 * the captured variables, are practically thrown away and not findable,
 * unless their class instances are cached somewhere.
 */
public class ClassCache {

    private Map<String, Class<? extends Unit>> unitClasses = new HashMap<> ();

    public void cacheClass (String name, Class<? extends Unit> cl) {
        if (unitClasses.containsKey (name)) {
            throw new RuntimeException ("Unit class " + name + " is already cached!");
        }

        unitClasses.put (name, cl);
    }

    public @Nullable Class<? extends Unit> getClass (String name) {
        return unitClasses.get (name);
    }

}
