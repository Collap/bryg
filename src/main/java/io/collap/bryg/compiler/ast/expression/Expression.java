package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.Node;

public abstract class Expression extends Node {

    protected Class<?> type;

    protected Expression (StandardVisitor visitor) {
        super (visitor);
    }

    public Class<?> getType () {
        return type;
    }

    public void setType (Class<?> type) {
        this.type = type;
    }

}
