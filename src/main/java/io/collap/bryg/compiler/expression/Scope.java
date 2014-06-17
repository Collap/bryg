package io.collap.bryg.compiler.expression;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private int nextId = 0;
    private Map<String, Variable> variables = new HashMap<> ();

    public Variable getVariable (String name) {
        return variables.get (name);
    }

    public Variable registerVariable (String name, Type type) {
        Variable variable = new Variable (type, name, calculateNextId (type));
        variables.put (name, variable);
        return variable;
    }

    public int calculateNextId (Type type) {
        int id = nextId;

        /* Double and long use two variable slots. */
        boolean isWide = false;
        if (type instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            isWide = primitiveType == PrimitiveType._long || primitiveType == PrimitiveType._double;
        }

        if (isWide) {
            nextId += 2;
        }else {
            nextId += 1;
        }

        return id;
    }

}
