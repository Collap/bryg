package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

public class BooleanLiteralExpression extends LiteralExpression {

    public BooleanLiteralExpression (Context context, BrygParser.BooleanLiteralContext ctx) {
        super (context, ctx.getStart ().getLine ());
        setType (Types.fromClass (Boolean.TYPE));

        value = ctx.value.getType () == BrygLexer.TRUE;
    }

    @Override
    public void compile () {
        /* Convert boolean to int, then load the constant. */
        int intValue = ((Boolean) value) ? 1 : 0;
        context.getMethodVisitor ().visitLdcInsn (intValue);
    }

}
