package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class FloatLiteralExpression extends LiteralExpression {

    public FloatLiteralExpression (CompilationContext compilationContext, BrygParser.FloatLiteralContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Float.TYPE));

        value = Float.parseFloat (ctx.Float ().getText ());
    }

}
