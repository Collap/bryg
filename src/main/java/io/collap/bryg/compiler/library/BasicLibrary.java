package io.collap.bryg.compiler.library;

import java.util.HashMap;
import java.util.Map;

public class BasicLibrary implements Library {

    private Map<String, Function> functions = new HashMap<> ();

    public BasicLibrary () {
        HTMLFunctions.register (this);
    }

    @Override
    public Function getFunction (String name) {
        return functions.get (name);
    }

    @Override
    public void setFunction (String name, Function function) {
        functions.put (name, function);
    }

}
