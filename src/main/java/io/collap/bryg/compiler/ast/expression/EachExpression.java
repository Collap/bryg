package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.expression.Scope;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class EachExpression extends Expression {

    private boolean isArray;
    private Variable iterator;
    private Variable element;
    private Expression collectionExpression;
    private Node statementOrBlock;

    public EachExpression (StandardVisitor visitor, BrygParser.EachExpressionContext ctx) {
        super (visitor);
        setType (new Type (Void.TYPE));

        collectionExpression = (Expression) visitor.visit (ctx.expression ());

        /* Open new scope. */
        Scope scope = visitor.getCurrentScope ().createSubScope ();
        visitor.setCurrentScope (scope);

        Type collectionType = collectionExpression.getType ();
        Type elementType = null;

        Class<?> elementClass = collectionType.getJavaType ().getComponentType (); /* Assuming the collection is an array. */
        if (elementClass != null) {
            isArray = true;
            elementType = new Type (elementClass);
        }else {
            isArray = false;

            /* Check for Iterable interface. */
            for (Class<?> itfClass : collectionType.getJavaType ().getInterfaces ()) {
                System.out.println (itfClass);
            }

            if (!Iterable.class.isAssignableFrom (collectionType.getJavaType ())) {
                throw new RuntimeException ("The collection needs to implement the Iterable interface!");
            }

            /* Check for generic type. */
            List<Type> genericTypes = collectionType.getGenericTypes ();
            if (genericTypes.size () <= 0) {
                throw new RuntimeException ("The element type of the collection could not be inferred!");
            }

            elementType = genericTypes.get (0);
        }

        if (elementType == null) {
            throw new RuntimeException ("The collection being iterated by the each expression is not a valid collection");
        }

        /* Register variable(s). */
        if (!isArray) {
            Type iteratorType = new Type (Iterator.class);
            iterator = new Variable (iteratorType, "", visitor.getRootScope ().calculateNextId (iteratorType));
        }

        String variableName = ctx.Id ().getText ();
        element = scope.registerVariable (variableName, elementType);

        statementOrBlock = visitor.visitStatementOrBlock (ctx.statementOrBlock ());

        /* Reset scope. */
        visitor.setCurrentScope (scope.getParent ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        if (isArray) {
            throw new UnsupportedOperationException ("The each expression is not implemented for arrays yet!");
        }else { /* Iterable. */
            Label conditionLabel = new Label ();
            Label blockLabel = new Label ();

            /* Get and store iterator. */
            collectionExpression.compile ();
            // -> Iterable

            method.visitMethodInsn (INVOKEINTERFACE, collectionExpression.getType ().getAsmType ().getInternalName (),
                    "iterator", TypeHelper.generateMethodDesc (null, Iterator.class), true);
            // Iterable -> Iterator

            method.visitVarInsn (ASTORE, iterator.getId ());
            method.visitFrame (F_APPEND, 1, new Object[] { iterator.getType ().getAsmType ().getInternalName () }, 0, null);
            // Iterator ->

            /* Jump to the condition. */
            method.visitJumpInsn (GOTO, conditionLabel);

            /* Block. */
            method.visitLabelInSameFrame (blockLabel);
            method.visitVarInsn (ALOAD, iterator.getId ());
            // -> Iterator

            method.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getAsmType ().getInternalName (),
                    "next", TypeHelper.generateMethodDesc (null, Object.class), true);
            // Iterator -> Object

            method.visitTypeInsn (CHECKCAST, element.getType ().getAsmType ().getInternalName ());
            // Object -> String

            method.visitVarInsn (ASTORE, element.getId ());
            method.visitFrame (F_APPEND, 1, new Object[] { element.getType ().getAsmType ().getInternalName () }, 0, null); /* Add local. */
            // T ->

            statementOrBlock.compile (); /* Use local. */

            method.visitFrame (F_CHOP, 1, null, 0, null); /* Remove local. */

            /* Condition. */
            method.visitLabelInSameFrame (conditionLabel);
            method.visitVarInsn (ALOAD, iterator.getId ());
            // -> Iterator

            method.visitMethodInsn (INVOKEINTERFACE, iterator.getType ().getAsmType ().getInternalName (),
                    "hasNext", TypeHelper.generateMethodDesc (null, new Type (Boolean.TYPE)), true);
            // Iterator -> int

            method.visitJumpInsn (IFNE, blockLabel); /* true: jump */
        }
    }

}
