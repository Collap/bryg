package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;

// TODO: Convert '\n', '\'', etc.

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (StandardVisitor visitor, String value, int line) {
        super (visitor);
        setType (new Type (String.class));
        setLine (line);
        this.value = value;
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> String
    }

    @Override
    public Object getConstantValue () {
        return value;
    }

}
