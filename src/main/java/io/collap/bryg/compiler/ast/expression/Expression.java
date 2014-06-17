package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.expression.Type;

public abstract class Expression extends Node {

    protected Type type;

    protected Expression (RenderVisitor visitor) {
        super (visitor);
    }

    public Type getType () {
        return type;
    }

    public void setType (Type type) {
        this.type = type;
    }

}
