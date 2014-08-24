package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.literal.StringLiteralExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

// TODO: Open new scope.

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
        /* Every statement which direct child expression returns a value and is not a variable
         * declaration is written automatically. */
        if (child instanceof Expression) {
            Expression expression = (Expression) child;
            Type type = expression.getType ();
            if (!type.equals (Void.TYPE) && !context.shouldDiscardPrintOutput ()) {
                BrygMethodVisitor mv = context.getMethodVisitor ();
                if (expression instanceof StringLiteralExpression) {
                    /* Append String constants to the constant string writer. */
                    String text = (String) expression.getConstantValue ();
                    mv.writeConstantString (text);
                }else {
                    // TODO: Does not work for double and long!

                    mv.loadWriter ();
                    // -> Writer

                    /* Stringify if necessary. */
                    if (type.getJavaType ().isPrimitive ()) {
                        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (mv);
                        stringBuilder.compileNew ();
                        stringBuilder.compileAppend (expression); // Note: The expression is compiled here!
                        stringBuilder.compileToString ();
                        // value -> String
                    } else {
                        expression.compile ();
                        // -> value

                        if (!type.equals (String.class)) {
                            mv.visitMethodInsn (INVOKEVIRTUAL, type.getAsmType ().getInternalName (), "toString",
                                    TypeHelper.generateMethodDesc (null, Void.TYPE), false);
                            // T -> String
                        }
                    }

                    mv.writeString ();
                    // Writer, value ->
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
