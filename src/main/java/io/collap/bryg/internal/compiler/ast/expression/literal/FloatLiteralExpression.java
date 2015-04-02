package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class FloatLiteralExpression extends LiteralExpression {

    public FloatLiteralExpression (Context context, BrygParser.FloatLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Float.TYPE));

        value = Float.parseFloat (ctx.Float ().getText ());
    }

}
