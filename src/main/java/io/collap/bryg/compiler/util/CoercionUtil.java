package io.collap.bryg.compiler.util;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;

public class CoercionUtil {

    private static class UnboxingExpression extends Expression {

        private Expression child;
        private Type boxedType;

        private UnboxingExpression (Context context, Expression child, Type unboxedType) {
            super (context);
            setLine (child.getLine ());
            setType (unboxedType);
            this.child = child;
            this.boxedType = child.getType ();
        }

        @Override
        public void compile () {
            child.compile ();
            // -> T

            BoxingUtil.compileUnboxing (context.getMethodVisitor (), boxedType, type);
            // T -> primitive
        }

    }

    /**
     * Assumes that the expressions have not been compiled yet.
     * The expressions are guaranteed to be compiled when the method returns properly (i.e. without throwing an exception);
     * The results are placed on the stack in the following order: left, right
     *
     * Unboxes boxed values.
     *
     * @throws io.collap.bryg.exception.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *         hence the compilation should be stopped at that point.
     */
    public static void attemptBinaryCoercion (Context context, Expression left, Expression right,
                                              Type targetType) {
        if (left.getType ().equals (right.getType ()) && left.getType ().equals (targetType)) {
            left.compile ();
            right.compile ();
            return;
        }

        if (!left.getType ().getJavaType ().isPrimitive ()) {
            left = getUnboxingExpressionOrThrowException (context, left);
        }

        if (!right.getType ().getJavaType ().isPrimitive ()) {
            right = getUnboxingExpressionOrThrowException (context, right);
        }

        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (left.getType ().isIntegralType () && right.getType ().isIntegralType ()) {
            /* This promotes all integers to longs. */
            promoteType (mv, left, right, targetType);
        }else if (left.getType ().isFloatingPointType () && right.getType ().isFloatingPointType ()) {
            /* This promotes all floats to doubles. */
            promoteType (mv, left, right, targetType);
        }else if (left.getType ().isIntegralType () && right.getType ().isFloatingPointType ()) {
            coerceIFP (mv, left, right, targetType, false);
        }else if (left.getType ().isFloatingPointType () && right.getType ().isIntegralType ()) {
            coerceIFP (mv, right, left, targetType, true);
        }else {
            throw new BrygJitException ("Coercion failed, but a target type was supplied.", left.getLine ());
        }
    }

    private static UnboxingExpression getUnboxingExpressionOrThrowException (Context context, Expression child) {
        Type unboxedType = BoxingUtil.unboxType (child.getType ());
        if (unboxedType == null) {
            throw new BrygJitException ("Can not coerce object types, but a target type was supplied.", child.getLine ());
        }

        return new UnboxingExpression (context, child, unboxedType);
    }

    /**
     * This method does not attempt to coerce object types, but accepts equal object types!
     * Boxed values are counted as unboxed. They are <b>not</b> unboxed by this method, though.
     *
     * @throws io.collap.bryg.exception.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *         hence the compilation should be stopped at that point.
     */
    public static Type getTargetType (Type leftType, Type rightType, int line) {
        if (leftType == null) throw new BrygJitException ("Left type is null.", line);
        if (rightType == null) throw new BrygJitException ("Right type is null.", line);

        if (leftType.equals (rightType)) {
            return leftType;
        }

        boolean attemptedToCoerceObjects = false;

        if (!leftType.getJavaType ().isPrimitive ()) {
            Type unboxedLeftType = BoxingUtil.unboxType (leftType);
            if (unboxedLeftType != null) {
                leftType = unboxedLeftType;
            }else {
                attemptedToCoerceObjects = true;
            }
        }

        if (!rightType.getJavaType ().isPrimitive ()) {
            Type unboxedRightType = BoxingUtil.unboxType (rightType);
            if (unboxedRightType != null) {
                rightType = unboxedRightType;
            }else {
                attemptedToCoerceObjects = true;
            }
        }

        if (attemptedToCoerceObjects) {
            throw new BrygJitException ("Can not coerce Object types!", line);
        }

        if (leftType.isIntegralType () && rightType.isIntegralType ()) {
            /* This promotes all integers to longs. */
            // TODO: What about left: byte and right: int for example?
            return new Type (Long.TYPE);
        }else if (leftType.isFloatingPointType () && rightType.isFloatingPointType ()) {
            /* This promotes all floats to doubles. */
            return new Type (Double.TYPE);
        }else if (leftType.isIntegralType () && rightType.isFloatingPointType ()) {
            return getTargetIFpType (leftType, rightType);
        }else if (leftType.isFloatingPointType () && rightType.isIntegralType ()) {
            return getTargetIFpType (rightType, leftType);
        }

        throw new BrygJitException ("Could not coerce " + leftType + " and " + rightType + " as this " +
                "combination is not currently supported!", line);
    }

    @Nullable
    private static Type getTargetIFpType (Type iType, Type fpType) {
        Type targetType = null;
        if (iType.equals (Integer.TYPE)) {
            if (fpType.equals (Float.TYPE)) {
                targetType = new Type (Float.TYPE);
            }else {
                targetType = new Type (Double.TYPE);
            }
        }else if (iType.equals (Long.TYPE)) {
            targetType = new Type (Double.TYPE);
        }
        return targetType;
    }

    /**
     * @param orderSwapped Whether the order of the expressions has been swapped. If so, they are swapped back before
     *                     they are compiled to retain the correct order of expressions.
     */
    private static Type coerceIFP (BrygMethodVisitor methodVisitor, Expression iExpr, Expression fpExpr,
                                   Type targetType, boolean orderSwapped) {
        if (targetType == null) {
            throw new BrygJitException ("Could not coerce " + iExpr.getType () + " and " + fpExpr.getType (),
                    iExpr.getLine ());
        }

        Expression left;
        Expression right;
        if (orderSwapped) {
            left = fpExpr;
            right = iExpr;
        }else {
            left = iExpr;
            right = fpExpr;
        }
        promoteType (methodVisitor, left, right, targetType);
        return targetType;
    }

    private static void promoteType (BrygMethodVisitor methodVisitor, Expression left, Expression right,
                                     Type targetType) {
        int leftConversionOp = NOP;
        int rightConversionOp = NOP;

        if (!left.getType ().equals (targetType)) {
            leftConversionOp = getConversionOpcode (left.getType (), targetType, left.getLine ());
        }
        if (!right.getType ().equals (targetType)) {
            rightConversionOp = getConversionOpcode (right.getType (), targetType, right.getLine ());
        }

        left.compile ();
        // -> T1

        if (leftConversionOp != NOP) {
            methodVisitor.visitInsn (leftConversionOp);
            // T1 -> T3
        }

        right.compile ();
        // T2

        if (rightConversionOp != NOP) {
            methodVisitor.visitInsn (rightConversionOp);
            // T2 -> T3
        }
    }

    /**
     * Returns the conversion opcode for a number of primitive conversions.
     * @param line Used for error reporting.
     */
    public static int getConversionOpcode (Type from, Type to, int line) {
        if (from.equals (Integer.TYPE)) {
            if (to.equals (Long.TYPE)) return I2L;
            if (to.equals (Float.TYPE)) return I2F;
            if (to.equals (Double.TYPE)) return I2D;
            if (to.equals (Byte.TYPE)) return I2B;
            if (to.equals (Short.TYPE)) return I2S;
            if (to.equals (Character.TYPE)) return I2C;
        }else if (from.equals (Long.TYPE)) {
            if (to.equals (Integer.TYPE)) return L2I;
            if (to.equals (Float.TYPE)) return L2F;
            if (to.equals (Double.TYPE)) return L2D;
        }else if (from.equals (Float.TYPE)) {
            if (to.equals (Integer.TYPE)) return F2I;
            if (to.equals (Long.TYPE)) return F2L;
            if (to.equals (Double.TYPE)) return F2D;
        }else if (from.equals (Double.TYPE)) {
            if (to.equals (Integer.TYPE)) return D2I;
            if (to.equals (Long.TYPE)) return D2L;
            if (to.equals (Float.TYPE)) return D2F;
        }

        throw new BrygJitException ("Conversion from " + from + " to " + to + " is not possible.", line);
    }

}
