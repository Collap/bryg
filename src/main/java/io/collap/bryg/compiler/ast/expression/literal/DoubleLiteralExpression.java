package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.parser.BrygParser;

public class DoubleLiteralExpression extends LiteralExpression {

    public DoubleLiteralExpression (Context context, BrygParser.DoubleLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Double.TYPE));

        value = Double.parseDouble (ctx.Double ().getText ());
    }

}
