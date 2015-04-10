package io.collap.bryg.internal.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.LocalVariable;
import io.collap.bryg.internal.type.*;
import io.collap.bryg.internal.compiler.StandardVisitor;
import io.collap.bryg.internal.Scope;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class EachStatement extends Node {

    private boolean isArray;
    private LocalVariable iterator;
    private LocalVariable element;
    private LocalVariable index;
    private Expression collectionExpression;
    private CompiledType collectionType;
    private Node statementOrBlock;

    public EachStatement (CompilationContext compilationContext, BrygParser.EachStatementContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        StandardVisitor ptv = compilationContext.getParseTreeVisitor ();

        BrygParser.EachHeadContext headCtx = ctx.eachHead ();

        collectionExpression = (Expression) ptv.visit (headCtx.expression ());

        /* Open new scope. */
        Scope scope = compilationContext.getCurrentScope ().createSubScope ();
        compilationContext.setCurrentScope (scope);

        if (!(collectionExpression.getType () instanceof CompiledType)) {
            throw new BrygJitException ("Can't call a Java method on a non-Java type.", getLine ());
        }

        collectionType = ((CompiledType) collectionExpression.getType ());
        BrygParser.TypeContext typeContext = headCtx.type ();
        Type declaredElementType = null;
        if (typeContext != null) {
            declaredElementType = new TypeInterpreter (compilationContext.getEnvironment ().getClassResolver ()).interpretType (typeContext);
        }
        Type elementType = null;

        Class<?> elementClass = collectionType.getJavaType ().getComponentType (); /* Assuming the collection is an array. */
        if (elementClass != null) {
            isArray = true;
            elementType = Types.fromClass (elementClass);
        }else {
            isArray = false;

            /* Check for Iterable interface. */
            if (!Iterable.class.isAssignableFrom (collectionType.getJavaType ())) {
                throw new BrygJitException ("The collection needs to implement the Iterable interface!", getLine ());
            }

            /* Check for element type. */
            List<Type> genericTypes = collectionType.getGenericTypes ();
            if (genericTypes.size () > 0) {
                elementType = genericTypes.get (0);
            }
        }

        if (declaredElementType != null) {
            if (elementType == null) {
                elementType = declaredElementType;
            }else if (!declaredElementType.similarTo (elementType)) {
                throw new BrygJitException ("The inferred element type differs from the declared element type!", getLine ());
            }
        }else if (elementType == null) {
            throw new BrygJitException ("The element type of the collection could not be inferred!", getLine ());
        }

        /* Register variable(s). */
        if (!isArray) {
            /* Register iterator out of scope. */
            Type iteratorType = Types.fromClass (Iterator.class);
            iterator = new LocalVariable (iteratorType, "", false); /* Immutable. */
            iterator.setId (compilationContext.getFragmentScope().calculateNextId (iteratorType));
        }

        String variableName = IdUtil.idToString (headCtx.element);
        element = new LocalVariable (elementType, variableName, false); /* Immutable. */
        scope.registerLocalVariable (element);

        BrygParser.IdContext indexCtx = headCtx.index;
        if (indexCtx != null) {
            String indexName = IdUtil.idToString (indexCtx);
            index = new LocalVariable (Types.fromClass (Integer.TYPE), indexName, false); /* Immutable. */
            scope.registerLocalVariable (index);
        }

        BrygParser.StatementOrBlockContext statementOrBlockCtx = ctx.statementOrBlock ();
        if (statementOrBlockCtx != null)  {
            statementOrBlock = ptv.visitStatementOrBlock (statementOrBlockCtx);
        }else {
            statementOrBlock = ptv.visitBlock (ctx.block ());
        }

        /* Reset scope. */
        compilationContext.setCurrentScope (scope.getParent ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        if (isArray) {
            // TODO: Implement
            throw new UnsupportedOperationException ("The each expression is not implemented for arrays yet!");
        }else { /* Iterable. */
            Label conditionLabel = new Label ();
            Label blockLabel = new Label ();

            /* Get and store iterator. */
            collectionExpression.compile ();
            // -> Iterable

            mv.visitMethodInsn (INVOKEINTERFACE, collectionExpression.getType ().getInternalName (),
                    "iterator", TypeHelper.generateMethodDesc (null, Iterator.class), true);
            // Iterable -> Iterator

            mv.visitVarInsn (ASTORE, iterator.getId ());
            // Iterator ->

            /* Init index variable (if needed). */
            if (index != null) {
                // TODO: Do we need to set it to zero? Check the JVM spec!
                mv.visitLdcInsn (0);
                // -> 0

                mv.visitVarInsn (ISTORE, index.getId ());
                // 0 ->
            }

            /* Jump to the condition. */
            mv.visitJumpInsn (GOTO, conditionLabel);

            /* Block. */
            mv.visitLabel (blockLabel);
            mv.visitVarInsn (ALOAD, iterator.getId ());
            // -> Iterator

            mv.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getInternalName (),
                    "next", TypeHelper.generateMethodDesc (null, Object.class), true);
            // Iterator -> Object

            mv.visitTypeInsn (CHECKCAST, element.getType ().getInternalName ());
            // Object -> String

            mv.visitVarInsn (ASTORE, element.getId ());
            // T ->

            statementOrBlock.compile (); /* Use local. */

            /* Increment index. */
            if (index != null) {
                mv.visitIincInsn (index.getId (), 1);
                // int -> int
            }

            /* Condition. */
            mv.visitLabel (conditionLabel);
            mv.visitVarInsn (ALOAD, iterator.getId ());
            // -> Iterator

            mv.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getInternalName (),
                    "hasNext", TypeHelper.generateMethodDesc (null, Types.fromClass (Boolean.TYPE)), true);
            // Iterator -> int

            mv.visitJumpInsn (IFNE, blockLabel); /* true: jump */
        }
    }

    @Override
    public void print (PrintStream out, int depth) {
        collectionExpression.print (out, depth + 1);
        statementOrBlock.print (out, depth + 1);
    }

}
