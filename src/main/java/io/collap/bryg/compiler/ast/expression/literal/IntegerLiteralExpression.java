package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends LiteralExpression {

    public IntegerLiteralExpression (Context context, BrygParser.IntegerLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (new Type (Integer.TYPE));

        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    public IntegerLiteralExpression (Context context, int value, int line) {
        super (context, line);
        setType (new Type (Integer.TYPE));

        this.value = value;
    }

}
