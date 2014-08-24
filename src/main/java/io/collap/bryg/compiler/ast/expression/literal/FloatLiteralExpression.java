package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class FloatLiteralExpression extends LiteralExpression {

    public FloatLiteralExpression (Context context, BrygParser.FloatLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (new Type (Float.TYPE));

        value = Float.parseFloat (ctx.Float ().getText ());
    }

}
