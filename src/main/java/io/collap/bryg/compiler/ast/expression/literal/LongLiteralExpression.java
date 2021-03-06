package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class LongLiteralExpression extends LiteralExpression {

    public LongLiteralExpression (Context context, BrygParser.LongLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (new Type (Long.TYPE));

        /* Remove suffix. */
        String valueString = ctx.Long ().getText ();
        if (valueString.endsWith ("L") || valueString.endsWith ("l")) {
            valueString = valueString.substring (0, valueString.length () - 1);
        }

        value = Long.parseLong (valueString);
    }

}
