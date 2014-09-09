package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Label;

import static bryg.org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;
    private Variable model;

    /**
     * An optional ('opt') input parameter may be null.
     */
    private boolean optional;

    public InDeclarationNode (Context context, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        this (
                context,
                IdUtil.idToString (ctx.id ()),
                new TypeInterpreter (context.getClassResolver ()).interpretType (ctx.type ()),
                ctx.qualifier.getType () == BrygLexer.OPT,
                ctx.getStart ().getLine ()
        );
    }

    public InDeclarationNode (Context context, String name, Type type, boolean optional, int line) {
        /**
         * In declarations are always immutable.
         */
        this (context, context.getCurrentScope ().registerVariable (name, type, true), optional, line);
    }

    public InDeclarationNode (Context context, Variable parameter, boolean optional, int line) {
        super (context);
        setLine (line);

        this.optional = optional;
        this.parameter = parameter;
        model = context.getCurrentScope ().getVariable ("model");

        /* A primitive may not be optional. */
        if (optional && parameter.getType ().getJavaType ().isPrimitive ()) {
            throw new BrygJitException ("A primitive input parameter may not be optional.", getLine ());
        }
    }

    @Override
    public void compile () {
        /* Get, check, cast and store variable. */
        loadVariable ();
        if (!optional) ifNullThrowException ();
        castAndStore ();
    }

    private void loadVariable () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitVarInsn (ALOAD, model.getId ());
        // -> Model

        mv.visitLdcInsn (parameter.getName ());
        // -> String

        mv.visitMethodInsn (INVOKEINTERFACE,
                model.getType ().getAsmType ().getInternalName (),
                "getVariable",
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { String.class },
                        Object.class
                ),
                true);
        // Model, String -> Object
    }

    private void ifNullThrowException () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitInsn (DUP);
        // Object -> Object, Object

        Label skipException = new Label ();
        mv.visitJumpInsn (IFNONNULL, skipException); /* Jump only when the reference is not null. */
        // Object ->

        /* Throw exception when the null check failed. */
        String exceptionInternalName = AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName ();
        mv.visitTypeInsn (NEW, exceptionInternalName);
        mv.visitInsn (DUP);
        // -> InvalidParameterException, InvalidParameterException

        mv.visitLdcInsn (parameter.getName () + " could not be loaded!");
        // -> String

        mv.visitMethodInsn (INVOKESPECIAL, exceptionInternalName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { String.class },
                        Void.TYPE
                ),
                false);
        // InvalidParameterException, String ->

        mv.visitInsn (ATHROW);
        // InvalidParameterException ->

        mv.visitLabel (skipException);
    }

    private void castAndStore () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        Type expectedType = parameter.getType ();
        Type boxedType = BoxingUtil.boxType (expectedType);

        if (boxedType != null) {
            mv.visitTypeInsn (CHECKCAST, boxedType.getAsmType ().getInternalName ());
            // Object -> T

            BoxingUtil.compileUnboxing (context.getMethodVisitor (), boxedType, expectedType);

            mv.visitVarInsn (expectedType.getAsmType ().getOpcode (ISTORE), parameter.getId ());
            // primitive ->
        }else {
            castAndStoreObject ();
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        String internalTypeName = parameter.getType ().getAsmType ().getInternalName ();

        mv.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        mv.visitVarInsn (ASTORE, parameter.getId ());
        // T ->
    }

}
