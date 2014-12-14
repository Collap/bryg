package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.Types;

// TODO: Convert '\n', '\'', etc.

public class StringLiteralExpression extends LiteralExpression {

    public StringLiteralExpression (Context context, String value, int line) {
        super (context, line);
        setType (Types.fromClass (String.class));

        this.value = value;
    }

}
