package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;

// TODO: Convert '\n', '\'', etc.

public class StringLiteralExpression extends LiteralExpression {

    public StringLiteralExpression (CompilationContext compilationContext, String value, int line) {
        super (compilationContext, line);
        setType (Types.fromClass (String.class));

        this.value = value;
    }

}
