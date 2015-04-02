package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class DoubleLiteralExpression extends LiteralExpression {

    public DoubleLiteralExpression (Context context, BrygParser.DoubleLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Double.TYPE));

        value = Double.parseDouble (ctx.Double ().getText ());
    }

}
