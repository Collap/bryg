package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.DummyExpression;
import io.collap.bryg.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.compiler.util.OperationUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.StandardUnit;

import static bryg.org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;
    private Variable model;
    private boolean isGlobalVariable;

    /**
     * An optional ('opt') input parameter may be null.
     */
    private boolean optional;

    public InDeclarationNode (Context context, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        this (
                context,
                IdUtil.idToString (ctx.id ()),
                new TypeInterpreter (context.getEnvironment ().getClassResolver ()).interpretType (ctx.type ()),
                ctx.qualifier.getType () == BrygLexer.OPT,
                ctx.getStart ().getLine ()
        );
    }

    public InDeclarationNode (Context context, String name, Type type, boolean optional, int line) {
        /**
         * In declarations are always immutable.
         */
        this (context, context.getCurrentScope ().registerVariable (new Variable (type, name, false, optional)),
                optional, line, false);
    }

    /**
     * @param isGlobalVariable Whether the variable needs to be loaded from the global variable model.
     */
    public InDeclarationNode (Context context, Variable parameter, boolean optional, int line, boolean isGlobalVariable) {
        super (context);
        setLine (line);

        this.optional = optional;
        this.parameter = parameter;
        this.isGlobalVariable = isGlobalVariable;
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
        // -> Object

        if (!optional) ifNullThrowException ();

        castAndStore ();
    }

    private void loadVariable () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (isGlobalVariable) {
            mv.visitVarInsn (ALOAD, 0);
            // -> this

            mv.visitFieldInsn (GETFIELD, new Type (StandardUnit.class).getAsmType ().getInternalName (),
                    "globalVariableModel", new Type (GlobalVariableModel.class).getAsmType ().getDescriptor ());
            // this -> GlobalVariableModel

            // TODO: Is this cast even needed?
            // mv.visitTypeInsn (CHECKCAST, new Type (Model.class).getAsmType ().getInternalName ());
            // GlobalVariableModel -> Model
        }else {
            mv.visitVarInsn (ALOAD, model.getId ());
            // -> Model
        }

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
        OperationUtil.compileIfNullThrowException (context.getMethodVisitor (),
                new Type (InvalidInputParameterException.class),
                parameter.getName () + " could not be loaded!");
    }

    // TODO: Coercion here?
    private void castAndStore () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        Type expectedType = parameter.getType ();
        Type boxedType = BoxingUtil.boxType (expectedType);

        if (boxedType != null) {
            mv.visitTypeInsn (CHECKCAST, boxedType.getAsmType ().getInternalName ());
            // Object -> T

            new UnboxingExpression (context, new DummyExpression (context, boxedType, getLine ()), expectedType).compile ();

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
