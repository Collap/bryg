package io.collap.bryg.internal.compiler.ast.expression.coercion;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import static bryg.org.objectweb.asm.Opcodes.DUP;
import static bryg.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static bryg.org.objectweb.asm.Opcodes.NEW;

public class BoxingExpression extends Expression {

    private Expression child;

    public BoxingExpression (Context context, Expression child, Type boxedType) {
        super (context);
        setLine (child.getLine ());
        setType (boxedType);
        this.child = child;
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        Type boxType = type;
        String boxTypeName = boxType.getInternalName ();
        Type paramType = child.getType ();

        /* There are no conversions from int to short or byte, so we just need to find the right constructor.
         * This allows to box bytes and shorts from int expressions. */
        if (paramType.similarTo (Integer.TYPE)) {
            if (boxType.similarTo (Byte.class)) {
                paramType = Types.fromClass (Byte.TYPE);
            }else if (boxType.similarTo (Short.class)) {
                paramType = Types.fromClass (Short.TYPE);
            }
        }

        mv.visitTypeInsn (NEW, boxTypeName);
        // -> T

        mv.visitInsn (DUP);
        // T -> T, T

        child.compile ();
        // -> primitive

        mv.visitMethodInsn (INVOKESPECIAL, boxTypeName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Type[] { paramType },
                        Types.fromClass (Void.TYPE)
                ),
                false);
        // T, primitive ->
    }

}
