package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.*;

// TODO: Evaluate constant expressions more efficiently.
// TODO: Subtraction?

public class BinaryAdditionExpression extends Expression {

    private Expression left;
    private Expression right;

    public BinaryAdditionExpression (StandardVisitor visitor, BrygParser.BinaryAdditionExpressionContext ctx) {
        super (visitor);
        setLine (ctx.getStart ().getLine ());

        left = (Expression) visitor.visit (ctx.expression (0));
        right = (Expression) visitor.visit (ctx.expression (1));
        if (left == null || right == null) {
            throw new BrygJitException ("Left or right is null: " + left + ", " + right, getLine ());
        }

        setupType ();
    }

    public BinaryAdditionExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor);
        this.left = left;
        this.right = right;
        setLine (line);

        setupType ();
    }

    private void setupType () {
        Type leftType = left.getType ();
        Type rightType = right.getType ();
        if (leftType.equals (String.class) || rightType.equals (String.class)) {
            setType (new Type (String.class));
        }else {
            setType (CoercionHelper.getTargetType (leftType, rightType, getLine ()));
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        /* Build String. */
        if (type.equals (String.class)) {
            buildString ();
            // -> String
        }else if (type.getJavaType ().isPrimitive ()) {
            CoercionHelper.attemptBinaryCoercion (method, left, right, type);
            compileAddInstruction ();
        }else {
            throw new BrygJitException ("Unexpected type " + type, getLine ());
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

    private void compileAddInstruction () {
        int op = type.getAsmType ().getOpcode (IADD);
        visitor.getMethod ().visitInsn (op);
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        left.print (out, depth + 1);
        right.print (out, depth + 1);
    }

}
