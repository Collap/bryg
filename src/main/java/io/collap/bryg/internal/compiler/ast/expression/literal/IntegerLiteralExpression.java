package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends LiteralExpression {

    public IntegerLiteralExpression (Context context, BrygParser.IntegerLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Integer.TYPE));

        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    public IntegerLiteralExpression (Context context, int value, int line) {
        super (context, line);
        setType (Types.fromClass (Integer.TYPE));

        this.value = value;
    }

}
