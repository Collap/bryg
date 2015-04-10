package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends LiteralExpression {

    public IntegerLiteralExpression (CompilationContext compilationContext, BrygParser.IntegerLiteralContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Integer.TYPE));

        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    public IntegerLiteralExpression (CompilationContext compilationContext, int value, int line) {
        super (compilationContext, line);
        setType (Types.fromClass (Integer.TYPE));

        this.value = value;
    }

}
