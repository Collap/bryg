package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.VariableDeclarationExpression;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.*;

public class StatementNode extends Node {

    private Node child;

    public StatementNode (StandardVisitor visitor, BrygParser.StatementContext ctx) {
        super (visitor);

        if (ctx.expression () != null) {
            child = visitor.visit (ctx.expression ());
            if (child == null) {
                throw new NullPointerException ("Expression in statement is null!");
            }
        }
    }

    @Override
    public void compile () {
        /* Every statement which direct child expression returns a value and is not a variable
         * declaration is written automatically. */
        if (child instanceof Expression) {
            Expression expression = (Expression) child;
            Class<?> type = expression.getType ();
            if (type != Void.TYPE && !(expression instanceof VariableDeclarationExpression)) {
                BrygMethodVisitor method = visitor.getMethod ();

                // TODO: Does not work for double and long!

                method.loadWriter ();
                // -> Writer

                /* Stringify if necessary. */
                if (type.isPrimitive ()) {
                    StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (visitor);
                    stringBuilder.compileNew ();
                    stringBuilder.compileAppend (expression); // Note: The expression is compiled here!
                    stringBuilder.compileToString ();
                    // value -> String
                }else {
                    expression.compile ();
                    // -> value

                    if (!type.equals (String.class)) {
                        method.visitMethodInsn (INVOKEVIRTUAL, Types.getAsmType (type).getInternalName (), "toString",
                                TypeHelper.generateMethodDesc (null, Void.TYPE), false);
                        // T -> String
                    }
                }

                method.writeString ();
                // Writer, value ->
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
