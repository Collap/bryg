package io.collap.bryg.compiler.ast.control;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.visitor.StandardVisitor;
import io.collap.bryg.compiler.scope.Scope;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class EachStatement extends Node {

    private boolean isArray;
    private Variable iterator;
    private Variable element;
    private Variable index;
    private Expression collectionExpression;
    private Node statementOrBlock;

    public EachStatement (Context context, BrygParser.EachStatementContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        StandardVisitor ptv = context.getParseTreeVisitor ();

        BrygParser.EachHeadContext headCtx = ctx.eachHead ();

        collectionExpression = (Expression) ptv.visit (headCtx.expression ());

        /* Open new scope. */
        Scope scope = context.getCurrentScope ().createSubScope ();
        context.setCurrentScope (scope);

        Type collectionType = collectionExpression.getType ();
        BrygParser.TypeContext typeContext = headCtx.type ();
        Type declaredElementType = null;
        if (typeContext != null) {
            declaredElementType = new TypeInterpreter (context.getEnvironment ().getClassResolver ()).interpretType (typeContext);
        }
        Type elementType = null;

        Class<?> elementClass = collectionType.getJavaType ().getComponentType (); /* Assuming the collection is an array. */
        if (elementClass != null) {
            isArray = true;
            elementType = new Type (elementClass);
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
            Type iteratorType = new Type (Iterator.class);
            iterator = new Variable (iteratorType, "", false); /* Immutable. */
            iterator.setId (context.getRootScope ().calculateNextId (iteratorType));
        }

        String variableName = IdUtil.idToString (headCtx.element);
        element = scope.registerVariable (new Variable (elementType, variableName, false)); /* Immutable. */

        BrygParser.IdContext indexCtx = headCtx.index;
        if (indexCtx != null) {
            String indexName = IdUtil.idToString (indexCtx);
            index = scope.registerVariable (new Variable (new Type (Integer.TYPE), indexName, false)); /* Immutable. */
        }

        BrygParser.StatementOrBlockContext statementOrBlockCtx = ctx.statementOrBlock ();
        if (statementOrBlockCtx != null)  {
            statementOrBlock = ptv.visitStatementOrBlock (statementOrBlockCtx);
        }else {
            statementOrBlock = ptv.visitBlock (ctx.block ());
        }

        /* Reset scope. */
        context.setCurrentScope (scope.getParent ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (isArray) {
            // TODO: Implement
            throw new UnsupportedOperationException ("The each expression is not implemented for arrays yet!");
        }else { /* Iterable. */
            Label conditionLabel = new Label ();
            Label blockLabel = new Label ();

            /* Get and store iterator. */
            collectionExpression.compile ();
            // -> Iterable

            mv.visitMethodInsn (INVOKEINTERFACE, collectionExpression.getType ().getAsmType ().getInternalName (),
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

            mv.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getAsmType ().getInternalName (),
                    "next", TypeHelper.generateMethodDesc (null, Object.class), true);
            // Iterator -> Object

            mv.visitTypeInsn (CHECKCAST, element.getType ().getAsmType ().getInternalName ());
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

            mv.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getAsmType ().getInternalName (),
                    "hasNext", TypeHelper.generateMethodDesc (null, new Type (Boolean.TYPE)), true);
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
