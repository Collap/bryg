package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Opcodes;

public class BinaryAdditionExpression extends BinaryArithmeticExpression {

    public BinaryAdditionExpression (StandardVisitor visitor, BrygParser.ExpressionContext leftCtx,
                                     BrygParser.ExpressionContext rightCtx) {
        super (visitor, leftCtx, rightCtx);
    }

    public BinaryAdditionExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
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
        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (visitor.getMethod ());

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
