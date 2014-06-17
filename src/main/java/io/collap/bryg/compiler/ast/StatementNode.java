package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.VariableDeclarationExpression;
import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.compiler.expression.Type;
import io.collap.bryg.compiler.util.TypeHelper;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.*;

public class StatementNode extends Node {

    private Node child;

    public StatementNode (RenderVisitor visitor, BrygParser.StatementContext ctx) {
        super (visitor);

        if (ctx.expression () != null) {
            child = visitor.visit (ctx.expression ());
            if (child == null) {
                throw new NullPointerException ("Expression in statement is null!");
            }
        }else { /* TextCommand. */
            child = visitor.visit (ctx.textCommand ());
        }
    }

    @Override
    public void compile () {
        /* Every statement which direct child expression returns a value and is not a variable
         * declaration is written automatically. */
        if (child instanceof Expression) {
            Expression expression = (Expression) child;
            Type type = expression.getType ();
            if (type != PrimitiveType._void && !(expression instanceof VariableDeclarationExpression)) {
                BrygMethodVisitor method = visitor.getMethod ();

                // TODO: Does not work for double and long!

                method.loadWriter ();
                // -> Writer

                /* Stringify if necessary. */
                if (type instanceof ClassType) {
                    expression.compile ();
                    // -> value

                    ClassType classType = (ClassType) type;
                    if (!classType.getActualType ().equals (String.class)) {
                        method.visitMethodInsn (INVOKEVIRTUAL, classType.getJvmName (), "toString",
                                TypeHelper.generateMethodDesc (null, PrimitiveType._void), false);
                        // T -> String
                    }
                }else { /* PrimitiveType. */
                    PrimitiveType primitiveType = (PrimitiveType) type;
                    StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper (visitor);
                    stringBuilder.compileNew ();
                    stringBuilder.compileAppend (expression); // Note: The expression is compiled here!
                    stringBuilder.compileToString ();
                    // value -> String
                }

                /* if (!(type instanceof ClassType) || !((ClassType) type).getActualType ().equals (String.class)) {
                    throw new UnsupportedOperationException ("Currently only Strings can be written (Type: "
                            + type + ", Expression class: " + child.getClass ().getSimpleName () + ")");
                } */

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
