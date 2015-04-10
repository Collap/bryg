package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class DoubleLiteralExpression extends LiteralExpression {

    public DoubleLiteralExpression (CompilationContext compilationContext, BrygParser.DoubleLiteralContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Double.TYPE));

        value = Double.parseDouble (ctx.Double ().getText ());
    }

}
