package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.Type;
import io.collap.bryg.parser.BrygParser;

import static org.objectweb.asm.Opcodes.*;

public class BinaryAdditionExpression extends Expression {

    private Expression left;
    private Expression right;

    public BinaryAdditionExpression (RenderVisitor renderVisitor, BrygParser.BinaryAdditionExpressionContext ctx) {
        super (renderVisitor);

        left = (Expression) renderVisitor.visit (ctx.expression (0));
        right = (Expression) renderVisitor.visit (ctx.expression (1));
        if (left == null || right == null) {
            throw new NullPointerException ("Left or right is null: " + left + ", " + right);
        }

        Type leftType = left.getType ();
        Type rightType = right.getType ();
        if (ClassType.isString (leftType) || ClassType.isString (rightType)) {
            setType (ClassType.STRING);
        }
    }

    @Override
    public void compile () {
        /* Build String. */
        if (ClassType.isString (type)) {
            buildString ();
        }
    }

    private void buildString () {
        BrygMethodVisitor method = visitor.getMethod ();

        ClassType stringBuilder;
        try {
            stringBuilder = new ClassType (visitor.getClassResolver ().getResolvedClass ("StringBuilder"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
            return;
        }

        method.loadWriter ();
        // -> Writer

        method.visitTypeInsn (NEW, stringBuilder.getJvmName ());
        // -> StringBuilder

        method.visitInsn (DUP);
        // StringBuilder -> StringBuilder, StringBuilder

        method.visitMethodInsn (INVOKESPECIAL, stringBuilder.getJvmName (), "<init>", "()V", false);
        // StringBuilder ->

        appendExpressionToString (left, stringBuilder);
        appendExpressionToString (right, stringBuilder);

        method.visitMethodInsn (INVOKEVIRTUAL, stringBuilder.getJvmName (), "toString", "()Ljava/lang/String;", false);
        // StringBuilder -> String

        method.writeString ();
        // Writer, String ->
    }

    private void appendExpressionToString (Expression expression, ClassType stringBuilder) {
        expression.compile ();
        visitor.getMethod ().visitMethodInsn (INVOKEVIRTUAL, stringBuilder.getJvmName (),
                "append", "(I)L" + stringBuilder.getJvmName () + ";", false);
        // StringBuilder, Object -> StringBuilder
    }

}
