package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

public class BooleanLiteralExpression extends LiteralExpression {

    public BooleanLiteralExpression (CompilationContext compilationContext, BrygParser.BooleanLiteralContext ctx) {
        super (compilationContext, ctx.getStart ().getLine ());
        setType (Types.fromClass (Boolean.TYPE));

        value = ctx.value.getType () == BrygLexer.TRUE;
    }

    @Override
    public void compile () {
        /* Convert boolean to int, then load the constant. */
        int intValue = ((Boolean) value) ? 1 : 0;
        compilationContext.getMethodVisitor ().visitLdcInsn (intValue);
    }

}
