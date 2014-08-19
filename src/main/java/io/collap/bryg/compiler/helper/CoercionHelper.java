package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;

import static org.objectweb.asm.Opcodes.*;

public class CoercionHelper {

    /**
     * Assumes that the expressions have not been compiled yet.
     * The expressions are guaranteed to be compiled when the method returns properly (i.e. without throwing an exception);
     * The results are placed on the stack in the following order: left, right
     *
     * Important note: This method does not attempt to coerce object types, but accepts equal object types!
     * @return The common type.
     * @throws io.collap.bryg.exception.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *          hence the compilation should be stopped at that point.
     */
    public static Type attemptBinaryCoercion (BrygMethodVisitor methodVisitor, Expression left, Expression right) {
        Type leftType = left.getType ();
        Type rightType = right.getType ();

        if (leftType.equals (rightType)) {
            left.compile ();
            right.compile ();
            return leftType;
        }

        if (!leftType.getJavaType ().isPrimitive () || !leftType.getJavaType ().isPrimitive ()) {
            throw new BrygJitException ("Can not coerce Object types!", left.getLine ());
        }

        if (leftType.isIntegralType () && rightType.isIntegralType ()) {
            /* This promotes all integers to longs. */
            // TODO: What about left: byte and right: int for example?
            Type targetType = new Type (Long.TYPE);
            promoteType (methodVisitor, left, right, targetType);
            return targetType;
        }else if (leftType.isFloatingPointType () && rightType.isFloatingPointType ()) {
            /* This promotes all floats to doubles. */
            Type targetType = new Type (Double.TYPE);
            promoteType (methodVisitor, left, right, targetType);
            return targetType;
        }else if (leftType.isIntegralType () && rightType.isFloatingPointType ()) {
            return coerceIFP (methodVisitor, left, right, false);
        }else if (leftType.isFloatingPointType () && rightType.isIntegralType ()) {
            return coerceIFP (methodVisitor, right, left, true);
        }

        throw new BrygJitException ("Could not coerce " + leftType + " and " + rightType + "as this " +
                "combination is not currently supported!", left.getLine ());
    }

    /**
     * @param orderSwapped Whether the order of the expressions has been swapped. If so, they are swapped back before
     *                     they are compiled to retain the correct order of expressions.
     */
    private static Type coerceIFP (BrygMethodVisitor methodVisitor, Expression iExpr, Expression fpExpr,
                                   boolean orderSwapped) {
        Type targetType = null;
        if (iExpr.getType ().equals (Integer.TYPE)) {
            if (fpExpr.getType ().equals (Float.TYPE)) {
                targetType = new Type (Float.TYPE);
            }else {
                targetType = new Type (Double.TYPE);
            }
        }else if (iExpr.getType ().equals (Long.TYPE)) {
            targetType = new Type (Double.TYPE);
        }

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
    private static int getConversionOpcode (Type from, Type to, int line) {
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

        throw new BrygJitException ("Could not convert from " + from + " to " + to, line);
    }

}
