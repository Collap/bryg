package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.library.html.HTMLFunctionCollection;

import java.util.HashMap;
import java.util.Map;

public class BasicLibrary implements Library {

    private Map<String, Function> functions = new HashMap<> ();

    public BasicLibrary () {
        /* Special global functions. */
        setFunction ("discard", new DiscardFunction ());

        /* HMTL functions. */
        new HTMLFunctionCollection (this).register ();
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
