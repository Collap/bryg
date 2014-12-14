package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.compiler.util.OperationUtil;
import io.collap.bryg.exception.InvalidInputParameterException;

import static bryg.org.objectweb.asm.Opcodes.*;
import static bryg.org.objectweb.asm.Opcodes.CHECKCAST;

public class ModelLoadExpression extends Expression {

    private VariableInfo info;
    private Variable model;

    /**
     *
     */
    public ModelLoadExpression (Context context, VariableInfo info, Variable model) {
        super (context);
        setLine (-1);
        setType (info.getType ());

        this.info = info;
        this.model = model;
    }

    @Override
    public void compile () {
        /* Get, check, cast and store variable. */
        loadVariable ();
        // -> Object

        if (!info.isNullable ()) ifNullThrowException ();

        cast ();
    }

    private void loadVariable () {
        BrygMethodVisitor mv = context.getMethodVisitor ();


        new VariableExpression (context, getLine (), model, AccessMode.get).compile ();
        // -> Model

        mv.visitLdcInsn (info.getName ());
        // -> String

        boolean isInterface = model.getType ().isInterface ();
        mv.visitMethodInsn (isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
                model.getType ().getInternalName (),
                "getVariable",
                TypeHelper.generateMethodDesc (
                        new Class<?>[]{String.class},
                        Object.class
                ),
                isInterface);
        // Model, String -> Object
    }

    private void ifNullThrowException () {
        OperationUtil.compileIfNullThrowException (context.getMethodVisitor (),
                Types.fromClass (InvalidInputParameterException.class),
                info.getName () + " could not be loaded!");
    }

    // TODO: Coercion here?
    private void cast () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        Type expectedType = info.getType ();
        Type wrapperType = expectedType.getWrapperType ();

        if (wrapperType != null) {
            mv.visitTypeInsn (CHECKCAST, wrapperType.getInternalName ());
            // Object -> T

            new UnboxingExpression (context, new DummyExpression (context, wrapperType, getLine ()), expectedType).compile ();
            // T -> primitive
        }else {
            mv.visitTypeInsn (CHECKCAST, info.getType ().getInternalName ());
            // Object -> T
        }
    }

}
