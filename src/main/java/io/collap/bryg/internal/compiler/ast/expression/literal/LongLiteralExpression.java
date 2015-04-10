package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class LongLiteralExpression extends LiteralExpression {

    public LongLiteralExpression (CompilationContext compilationContext, BrygParser.LongLiteralContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Long.TYPE));

        /* Remove suffix. */
        String valueString = ctx.Long ().getText ();
        if (valueString.endsWith ("L") || valueString.endsWith ("l")) {
            valueString = valueString.substring (0, valueString.length () - 1);
        }

        value = Long.parseLong (valueString);
    }

}
