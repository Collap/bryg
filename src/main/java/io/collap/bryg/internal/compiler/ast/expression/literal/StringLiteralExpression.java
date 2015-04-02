package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.type.Types;

// TODO: Convert '\n', '\'', etc.

public class StringLiteralExpression extends LiteralExpression {

    public StringLiteralExpression (Context context, String value, int line) {
        super (context, line);
        setType (Types.fromClass (String.class));

        this.value = value;
    }

}
