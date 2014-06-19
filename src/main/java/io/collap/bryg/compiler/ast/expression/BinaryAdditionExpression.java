package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

public class BinaryAdditionExpression extends Expression {

    private Expression left;
    private Expression right;

    public BinaryAdditionExpression (StandardVisitor visitor, BrygParser.BinaryAdditionExpressionContext ctx) {
        super (visitor);

        left = (Expression) visitor.visit (ctx.expression (0));
        right = (Expression) visitor.visit (ctx.expression (1));
        if (left == null || right == null) {
            throw new NullPointerException ("Left or right is null: " + left + ", " + right);
        }

        Class<?> leftType = left.getType ();
        Class<?> rightType = right.getType ();
        if (leftType.equals (String.class) || rightType.equals (String.class)) {
            setType (String.class);
        }
    }

    @Override
    public void compile () {
        /* Build String. */
        if (type.equals (String.class)) {
            buildString ();
        }
    }

    private void buildString () {
        BrygMethodVisitor method = visitor.getMethod ();

        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (visitor);

        method.loadWriter ();
        // -> Writer

        stringBuilder.compileNew ();
        stringBuilder.compileAppend (left);
        stringBuilder.compileAppend (right);
        // -> StringBuilder

        stringBuilder.compileToString ();
        // StringBuilder -> String

        method.writeString ();
        // Writer, String ->
    }

}
