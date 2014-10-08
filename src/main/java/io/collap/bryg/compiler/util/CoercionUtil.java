package io.collap.bryg.compiler.util;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Replace current dual coercion mechanism (find type first, then attempt coercion in compile method) with
// a mechanism that wraps expressions in coercion nodes. This should reduce redundant code and computation.

public class CoercionUtil {

    /**
     *  Takes 'left' and 'right' and tries to generate expressions that cast, unbox and box them.
     *
     * Returns a pair of expressions, with 'left' being a child of or being the first element of the tuple,
     * and 'right' being a child of or being the second element of the tuple.
     *
     * Unboxes boxed values.
     *
     * @throws io.collap.bryg.exception.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *         hence the compilation should be stopped at that point.
     */
    public static Pair<Expression, Expression> applyBinaryCoercion (Context context, Expression left, Expression right) {
        if (left == null) throw new BrygJitException ("The left is null. This is an internal compiler issue.", -1); // TODO: Get line...
        if (right == null) throw new BrygJitException ("The right type is null. This is an internal compiler issue.", -1);

        /* Attempt unboxing. */
        if (!left.getType ().getJavaType ().isPrimitive ()) {
            left = getUnboxingExpressionOrThrowException (context, left);
        }

        if (!right.getType ().getJavaType ().isPrimitive ()) {
            right = getUnboxingExpressionOrThrowException (context, right);
        }

        /* Get target type. */
        Type targetType = getTargetType (left.getType (), right.getType ());

        /* Return if we don't even need to convert anything. */
        if (left.getType ().similarTo (right.getType ()) && left.getType ().similarTo (targetType)) {
            return new Pair<> (left, right);
        }

        if (left.getType ().isIntegralType () && right.getType ().isIntegralType ()) {
            return promoteType (context, left, right, targetType);
        }else if (left.getType ().isFloatingPointType () && right.getType ().isFloatingPointType ()) {
            return promoteType (context, left, right, targetType);
        }else if (left.getType ().isIntegralType () && right.getType ().isFloatingPointType ()) {
            return promoteType (context, left, right, targetType);
        }else if (left.getType ().isFloatingPointType () && right.getType ().isIntegralType ()) {
            return promoteType (context, right, left, targetType);
        }else {
            throw new BrygJitException ("Coercion failed, but a target type was supplied: " + targetType.getJavaType (), left.getLine ());
        }
    }

    private static UnboxingExpression getUnboxingExpressionOrThrowException (Context context, Expression child) {
        Type unboxedType = BoxingUtil.unboxType (child.getType ());
        if (unboxedType == null) {
            throw new BrygJitException ("Can not coerce object types, but a target type was supplied.", child.getLine ());
        }

        return new UnboxingExpression (context, child, unboxedType);
    }

    private static Pair<Expression, Expression> promoteType (Context context, Expression left, Expression right,
                                                             Type targetType) {

        if (!left.getType ().similarTo (targetType)) {
            left = new CastExpression (context, targetType, left, left.getLine ());
        }

        if (!right.getType ().similarTo (targetType)) {
            right = new CastExpression (context, targetType, right, right.getLine ());
        }

        return new Pair<> (left, right);
    }

    /**
     * Expects the types to be unboxed already.
     *
     * @return null if no target type can be found.
     */
    private static Type getTargetType (Type leftType, Type rightType) {
        if (leftType.similarTo (rightType)) {
            return leftType;
        }

        if (leftType.isIntegralType () && rightType.isIntegralType ()) {
            if (leftType.similarTo (Long.TYPE) || rightType.similarTo (Long.TYPE)) {
                /* This promotes any integers to longs. */
                return new Type (Long.TYPE);
            }else {
                /* Both type are integers or smaller, so no need for longs.*/
                return new Type (Integer.TYPE);
            }
        }else if (leftType.isFloatingPointType () && rightType.isFloatingPointType ()) {
            /* This promotes all floats to doubles.
             * We already know that the types are not equal, so these two types can't possibly
             * both be floats.*/
            return new Type (Double.TYPE);
        }else if (leftType.isIntegralType () && rightType.isFloatingPointType ()) {
            return getTargetIFpType (leftType, rightType);
        }else if (leftType.isFloatingPointType () && rightType.isIntegralType ()) {
            return getTargetIFpType (rightType, leftType);
        }

        return null;
    }

    @Nullable
    private static Type getTargetIFpType (Type iType, Type fpType) {
        if (!iType.isIntegralType ()) return null;
        if (!fpType.isFloatingPointType ()) return null;

        if (iType.similarTo (Long.TYPE)) {
            return new Type (Double.TYPE);
        }else { /* byte, int, short */
            if (fpType.similarTo (Float.TYPE)) {
                return new Type (Float.TYPE);
            }else {
                return new Type (Double.TYPE);
            }
        }
    }

    /**
     * Returns 'expr' or a new Expression with 'expr' as a child.
     *
     * Unboxes boxed values.
     * Automatically boxes the value if the target type is a box.
     *
     * @throws io.collap.bryg.exception.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *         hence the compilation should be stopped at that point.
     */
    public static Expression applyUnaryCoercion (Context context, Expression expr, Type targetType) {
        if (!expr.getType ().getJavaType ().isPrimitive ()) {
            expr = getUnboxingExpressionOrThrowException (context, expr);
        }

        Type boxedType = null;
        if (BoxingUtil.isBoxedType (targetType)) {
            boxedType = targetType;
            targetType = BoxingUtil.unboxType (targetType);
        }

        int conversionOpcode = getUnaryConversionOpcode (expr, targetType);
        if (conversionOpcode == NOP - 1) {
            throw new BrygJitException ("Conversion from " + expr.getType () + " to " + targetType + " is not possible.", expr.getLine ());
        }

        /* Convert if needed. */
        if (conversionOpcode != NOP) {
            expr = new CastExpression (context, targetType, expr, conversionOpcode, expr.getLine ());
        }

        /* Box if needed. */
        if (boxedType != null) {
            expr = BoxingUtil.createBoxingExpression (context, expr);

            if (!boxedType.similarTo (expr.getType ())) {
                throw new BrygJitException ("Boxed types do not match: " + boxedType.getJavaType () + ", "
                        + expr.getType ().getJavaType (), expr.getLine ());
            }
        }

        return expr;
    }

    /**
     * You need to make sure that all types are properly unboxed.
     *
     * @return NOP - 1 when no opcode has been found. You need to check for this.
     */
    private static int getUnaryConversionOpcode (Expression expr, Type target) {
        if (expr.getType ().similarTo (target)) return NOP;

        if (expr.getType ().similarTo (Integer.TYPE)) {
            /* Special case: Constant expressions that return the type int and have a value in byte or short bounds
             * can be demoted to byte or short, respectively. */
            if (expr.isConstant ()) {
                int value = ((Integer) expr.getConstantValue ());
                if (target.similarTo (Byte.TYPE)) {
                    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                        return I2B;
                    }
                } else if (target.similarTo (Short.TYPE)) {
                    if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                        return I2S;
                    }
                }
            }
        }

        return getPromotionOpcode (expr.getType (), target);
    }

    /**
     * Returns the conversion opcode for a number of primitive conversions.
     * @param line Used for error reporting.
     * @return May return NOP if the "conversion" is implicit.
     */
    public static int getConversionOpcode (Type from, Type to, int line) {
        int opcode = getDemotionOpcode (from, to);
        if (opcode == NOP - 1) {
            opcode = getPromotionOpcode (from, to);
        }

        if (opcode == NOP - 1) {
            throw new BrygJitException ("Conversion from " + from + " to " + to + " is not possible.", line);
        }

        return opcode;
    }

    /**
     * @return NOP - 1 if no opcode has been found. You need to check for this.
     */
    public static int getDemotionOpcode (Type from, Type to) {
        if (from.similarTo (Short.TYPE)) {
            if (to.similarTo (Byte.TYPE)) return I2B;
        }else if (from.similarTo (Integer.TYPE)) {
            if (to.similarTo (Byte.TYPE)) return I2B;
            if (to.similarTo (Short.TYPE)) return I2S;
            if (to.similarTo (Character.TYPE)) return I2C;
        }else if (from.similarTo (Long.TYPE)) {
            if (to.similarTo (Integer.TYPE)) return L2I;
        }else if (from.similarTo (Float.TYPE)) {
            if (to.similarTo (Integer.TYPE)) return F2I;
            if (to.similarTo (Long.TYPE)) return F2L;
        }else if (from.similarTo (Double.TYPE)) {
            if (to.similarTo (Integer.TYPE)) return D2I;
            if (to.similarTo (Long.TYPE)) return D2L;
            if (to.similarTo (Float.TYPE)) return D2F;
        }

        return NOP - 1;
    }

    /**
     * @return NOP - 1 if no opcode has been found. You need to check for this.
     */
    public static int getPromotionOpcode (Type from, Type to) {
        if (from.similarTo (Byte.TYPE)) {
            if (to.similarTo (Short.TYPE)) return NOP;
            if (to.similarTo (Integer.TYPE)) return NOP;
            if (to.similarTo (Long.TYPE)) return I2L;
            if (to.similarTo (Float.TYPE)) return I2F;
            if (to.similarTo (Double.TYPE)) return I2D;
            if (to.similarTo (Character.TYPE)) return I2C;
        }else if (from.similarTo (Short.TYPE)) {
            if (to.similarTo (Integer.TYPE)) return NOP;
            if (to.similarTo (Long.TYPE)) return I2L;
            if (to.similarTo (Float.TYPE)) return I2F;
            if (to.similarTo (Double.TYPE)) return I2D;
            if (to.similarTo (Character.TYPE)) return I2C;
        }else if (from.similarTo (Integer.TYPE)) {
            if (to.similarTo (Long.TYPE)) return I2L;
            if (to.similarTo (Float.TYPE)) return I2F;
            if (to.similarTo (Double.TYPE)) return I2D;
            if (to.similarTo (Character.TYPE)) return I2C;
        }else if (from.similarTo (Long.TYPE)) {
            if (to.similarTo (Float.TYPE)) return L2F;
            if (to.similarTo (Double.TYPE)) return L2D;
        }else if (from.similarTo (Float.TYPE)) {
            if (to.similarTo (Double.TYPE)) return F2D;
        }

        return NOP - 1;
    }

}
