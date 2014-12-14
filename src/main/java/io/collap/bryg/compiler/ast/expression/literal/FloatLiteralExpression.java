package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.parser.BrygParser;

public class FloatLiteralExpression extends LiteralExpression {

    public FloatLiteralExpression (Context context, BrygParser.FloatLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Float.TYPE));

        value = Float.parseFloat (ctx.Float ().getText ());
    }

}
