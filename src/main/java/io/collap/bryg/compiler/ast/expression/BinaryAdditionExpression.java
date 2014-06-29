package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

// TODO: Evaluate constant expressions more efficiently.

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

        Type leftType = left.getType ();
        Type rightType = right.getType ();
        if (leftType.equals (String.class) || rightType.equals (String.class)) {
            setType (new Type (String.class));
        }
    }

    @Override
    public void compile () {
        /* Build String. */
        if (type.equals (String.class)) {
            buildString ();
            // -> String
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
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        left.print (out, depth + 1);
        right.print (out, depth + 1);
    }

}
