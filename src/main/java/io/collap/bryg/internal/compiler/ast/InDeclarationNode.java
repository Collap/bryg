package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.ast.expression.DummyExpression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.compiler.util.OperationUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.InvalidInputParameterException;
import io.collap.bryg.GlobalVariableModel;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.internal.StandardUnit;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Only compile if parameter is actually used.

@Deprecated
public class InDeclarationNode extends Node {

    private Variable parameter;
    private Variable model;
    private boolean isGlobalVariable;

    /**
     * An optional ('opt') input parameter may be null.
     */
    private boolean optional;

    private InDeclarationNode (CompilationContext compilationContext, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        this (
                compilationContext,
                IdUtil.idToString (ctx.id ()),
                new TypeInterpreter (compilationContext.getEnvironment ().getClassResolver ()).interpretType (ctx.type ()),
                ctx.qualifier.getType () == BrygLexer.OPT,
                ctx.getStart ().getLine ()
        );
    }

    private InDeclarationNode (CompilationContext compilationContext, String name, Type type, boolean optional, int line) {
        /**
         * In declarations are always immutable.
         */
        super (compilationContext);
        /* this (context, context.getCurrentScope ().registerVariable (new LocalVariable (type, name, false, optional)),
                optional, line, false); */
    }

    /**
     * @param isGlobalVariable Whether the variable needs to be loaded from the global variable model.
     */
    private InDeclarationNode (CompilationContext compilationContext, Variable parameter, boolean optional, int line, boolean isGlobalVariable) {
        super (compilationContext);
        setLine (line);

        this.optional = optional;
        this.parameter = parameter;
        this.isGlobalVariable = isGlobalVariable;
        model = compilationContext.getCurrentScope ().getVariable ("model");

        /* A primitive may not be optional. */
        if (optional && parameter.getType ().isPrimitive ()) {
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
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        if (isGlobalVariable) {
            mv.visitVarInsn (ALOAD, 0);
            // -> this

            mv.visitFieldInsn (GETFIELD, Types.fromClass (StandardUnit.class).getInternalName (),
                    "globalVariableModel", Types.fromClass (GlobalVariableModel.class).getDescriptor ());
            // this -> GlobalVariableModel

            // TODO: Is this cast even needed?
            // mv.visitTypeInsn (CHECKCAST, new Type (Model.class).getAsmType ().getInternalName ());
            // GlobalVariableModel -> Model
        }else {
            new VariableExpression (compilationContext, getLine (), model, AccessMode.get).compile ();
            // -> Model
        }

        mv.visitLdcInsn (parameter.getName ());
        // -> String

        mv.visitMethodInsn (INVOKEINTERFACE,
                model.getType ().getInternalName (),
                "getVariable",
                TypeHelper.generateMethodDesc (
                        new Class<?>[]{String.class},
                        Object.class
                ),
                true);
        // Model, String -> Object
    }

    private void ifNullThrowException () {
        OperationUtil.compileIfNullThrowException (compilationContext.getMethodVisitor (),
                Types.fromClass (InvalidInputParameterException.class),
                parameter.getName () + " could not be loaded!");
    }

    // TODO: Coercion here?
    private void castAndStore () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        Type expectedType = parameter.getType ();
        Type wrapperType = expectedType.getWrapperType ();

        if (wrapperType != null) {
            mv.visitTypeInsn (CHECKCAST, wrapperType.getInternalName ());
            // Object -> T

            new UnboxingExpression (compilationContext, new DummyExpression (compilationContext, wrapperType, getLine ()), expectedType).compile ();

            // new VariableExpression (context, getLine (), parameter, AccessMode.set, ).compile ();
            // primitive ->
        }else {
            castAndStoreObject ();
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();
        String internalTypeName = parameter.getType ().getInternalName ();

        mv.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        // mv.visitVarInsn (ASTORE, parameter.getId ());
        // T ->
    }

}
