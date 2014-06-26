package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.type.Type;

public abstract class Expression extends Node {

    protected Type type;

    protected Expression (StandardVisitor visitor) {
        super (visitor);
    }

    public Type getType () {
        return type;
    }

    public void setType (Type type) {
        this.type = type;
    }

    /**
     * @return If not null, the type of the value is guaranteed to be an instance of the type returned by {@link #getType()}.
     */
    public Object getConstantValue () {
        return null;
    }

}
