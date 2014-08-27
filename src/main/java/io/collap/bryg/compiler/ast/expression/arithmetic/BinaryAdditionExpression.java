package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Opcodes;

public class BinaryAdditionExpression extends BinaryArithmeticExpression {

    public BinaryAdditionExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                     BrygParser.ExpressionContext rightCtx) {
        super (context, leftCtx, rightCtx);
    }

    public BinaryAdditionExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected void setupType () {
        if (left.getType ().equals (String.class) || right.getType ().equals (String.class)) {
            setType (new Type (String.class));
        }else {
            super.setupType ();
        }
    }

    @Override
    public void compile () {
        /* Build String. */
        if (type.equals (String.class)) {
            buildString ();
            // -> String
        }else {
            super.compile ();
        }
    }

    private void buildString () {
        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (context.getMethodVisitor ());

        stringBuilder.compileNew ();
        stringBuilder.compileAppend (left);
        stringBuilder.compileAppend (right);
        // -> StringBuilder

        stringBuilder.compileToString ();
        // StringBuilder -> String
    }


    @Override
    protected int getOpcode () {
        return Opcodes.IADD;
    }

}
