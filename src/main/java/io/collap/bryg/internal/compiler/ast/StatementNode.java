package io.collap.bryg.internal.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.literal.StringLiteralExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.util.StringBuilderCompileHelper;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static bryg.org.objectweb.asm.Opcodes.POP;

public class StatementNode extends Node {

    private Node child;

    public StatementNode (Context context, BrygParser.StatementContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        child = context.getParseTreeVisitor ().visit (ctx.getChild (0));
        if (child == null) {
            throw new BrygJitException ("Child of statement was expected but is null!", getLine ());
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        /* Give ASM a line number. */
        Label here = new Label ();
        mv.visitLabel (here);
        mv.visitLineNumber (getLine (), here);

        /* Every statement which direct child expression returns a value and is not a variable
         * declaration is written automatically. */
        if (child instanceof Expression) {
            Expression expression = (Expression) child;
            Type type = expression.getType ();
            if (!type.similarTo (Void.TYPE)) {
                if (expression instanceof StringLiteralExpression) {
                    /* Append String constants to the constant string writer. */
                    String text = (String) expression.getConstantValue ();
                    mv.writeConstantString (text);
                }else {
                    if (!context.shouldDiscardPrintOutput ()) {
                        mv.loadWriter ();
                        // -> Writer

                        /* Stringify if necessary. */
                        if (type.isPrimitive ()) {
                            StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (mv);
                            stringBuilder.compileNew ();
                            stringBuilder.compileAppend (expression); // Note: The expression is compiled here!
                            stringBuilder.compileToString ();
                            // value -> String
                        } else {
                            expression.compile ();
                            // -> value

                            if (!type.similarTo (String.class)) {
                                mv.visitMethodInsn (INVOKEVIRTUAL, type.getInternalName (), "toString",
                                        TypeHelper.generateMethodDesc (null, String.class), false);
                                // T -> String
                            }
                        }

                        mv.writeString ();
                        // Writer, value ->
                    }else {
                        expression.compile ();
                        // -> T

                        /* Instead of being written, the value needs to be popped. */
                        mv.visitInsn (POP);
                        // T ->
                    }
                }
            }else {
                child.compile ();
            }
        }else {
            child.compile ();
        }
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        child.print (out, depth + 1);
    }

}
