package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.internal.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.BrygJitException;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;

public class CoercionUtil {

    /**
     * Takes 'left' and 'right' and tries to generate expressions that cast, unbox and box them.
     * <p>
     * Returns a pair of expressions, with 'left' being a child of or being the first element of the tuple,
     * and 'right' being a child of or being the second element of the tuple.
     * <p>
     * Unboxes boxed values.
     * <p>
     * Does nothing with objects.
     *
     * @throws io.collap.bryg.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *                                         hence the compilation should be stopped at that point.
     */
    public static Pair<Expression, Expression> applyBinaryCoercion(CompilationContext compilationContext,
                                                                   @Nullable Expression left, @Nullable Expression right) {
        // TODO: Get line...
        if (left == null) {
            throw new BrygJitException("The left is null. This is probably an internal compiler issue.", Node.UNKNOWN_LINE);
        }

        if (right == null) {
            throw new BrygJitException("The right type is null. This is probably an internal compiler issue.", Node.UNKNOWN_LINE);
        }

        /* Immediately return objects, like Strings. Ignore primitives and boxes. */
        if (!left.getType().isPrimitive() && !right.getType().isPrimitive()) {
            @Nullable Type leftPrimitive = left.getType().getPrimitiveType();
            @Nullable Type rightPrimitive = right.getType().getPrimitiveType();
            if (leftPrimitive == null && rightPrimitive == null) {
                // TODO: Widening reference conversion here? The current use cases don't need it.
                return new Pair<>(left, right);
            }
        }

        /* Attempt unboxing. */
        if (!left.getType().isPrimitive()) {
            left = getUnboxingExpressionOrThrowException(compilationContext, left);
        }

        if (!right.getType().isPrimitive()) {
            right = getUnboxingExpressionOrThrowException(compilationContext, right);
        }

        /* Get target type. */
        Type targetType = getTargetType(left.getType(), right.getType());

        /* Return if we don't even need to convert anything. */
        if (left.getType().similarTo(right.getType()) && left.getType().similarTo(targetType)) {
            return new Pair<>(left, right);
        }

        if (left.getType().isIntegralType() && right.getType().isIntegralType()) {
            return promoteType(compilationContext, left, right, targetType);
        } else if (left.getType().isFloatingPointType() && right.getType().isFloatingPointType()) {
            return promoteType(compilationContext, left, right, targetType);
        } else if (left.getType().isIntegralType() && right.getType().isFloatingPointType()) {
            return promoteType(compilationContext, left, right, targetType);
        } else if (left.getType().isFloatingPointType() && right.getType().isIntegralType()) {
            return promoteType(compilationContext, right, left, targetType);
        } else {
            throw new BrygJitException("Coercion failed, but a target type was supplied: " + targetType, left.getLine());
        }
    }

    private static UnboxingExpression getUnboxingExpressionOrThrowException(CompilationContext compilationContext, Expression child) {
        @Nullable Type primitiveType = child.getType().getPrimitiveType();
        if (primitiveType == null) {
            throw new BrygJitException("Can not coerce object types, but a target type was supplied.", child.getLine());
        }

        return new UnboxingExpression(compilationContext, child, primitiveType);
    }

    private static Pair<Expression, Expression> promoteType(CompilationContext compilationContext, Expression left, Expression right,
                                                            Type targetType) {

        if (!left.getType().similarTo(targetType)) {
            left = new CastExpression(compilationContext, targetType, left, left.getLine());
        }

        if (!right.getType().similarTo(targetType)) {
            right = new CastExpression(compilationContext, targetType, right, right.getLine());
        }

        return new Pair<>(left, right);
    }

    /**
     * Expects the types to be unboxed already.
     *
     * @return null if no target type can be found.
     */
    private static @Nullable Type getTargetType(Type leftType, Type rightType) {
        if (leftType.similarTo(rightType)) {
            return leftType;
        }

        if (leftType.isIntegralType() && rightType.isIntegralType()) {
            if (leftType.similarTo(Long.TYPE) || rightType.similarTo(Long.TYPE)) {
                /* This promotes any integers to longs. */
                return Types.fromClass(Long.TYPE);
            } else {
                /* Both type are integers or smaller, so no need for longs.*/
                return Types.fromClass(Integer.TYPE);
            }
        } else if (leftType.isFloatingPointType() && rightType.isFloatingPointType()) {
            /* This promotes all floats to doubles.
             * We already know that the types are not equal, so these two types can't possibly
             * both be floats.*/
            return Types.fromClass(Double.TYPE);
        } else if (leftType.isIntegralType() && rightType.isFloatingPointType()) {
            return getTargetIFpType(leftType, rightType);
        } else if (leftType.isFloatingPointType() && rightType.isIntegralType()) {
            return getTargetIFpType(rightType, leftType);
        }

        return null;
    }

    private static @Nullable Type getTargetIFpType(Type iType, Type fpType) {
        if (!iType.isIntegralType()) return null;
        if (!fpType.isFloatingPointType()) return null;

        if (iType.similarTo(Long.TYPE)) {
            return Types.fromClass(Double.TYPE);
        } else { /* byte, int, short */
            if (fpType.similarTo(Float.TYPE)) {
                return Types.fromClass(Float.TYPE);
            } else {
                return Types.fromClass(Double.TYPE);
            }
        }
    }

    /**
     * This method catches any exceptions thrown by {@link #applyUnaryCoercion(io.collap.bryg.internal.compiler.CompilationContext,
     * io.collap.bryg.internal.compiler.ast.expression.Expression, io.collap.bryg.internal.Type)} and returns null
     * in those cases.
     */
    public static @Nullable Expression tryUnaryCoercion(CompilationContext compilationContext, Expression expr, Type targetType) {
        try {
            return applyUnaryCoercion(compilationContext, expr, targetType);
        } catch (BrygJitException ex) {
            return null;
        }
    }

    /**
     * Returns 'expr' or a new Expression with 'expr' as an (indirect) child.
     * <p>
     * Unboxes boxed values.
     * Automatically boxes the value if the target type is a box.
     *
     * @throws io.collap.bryg.BrygJitException When the types can't be coerced. The behaviour in this case is undefined,
     *                                         hence the compilation should be stopped at that point.
     */
    public static Expression applyUnaryCoercion(CompilationContext compilationContext, Expression expr, Type targetType) {
        if (expr.getType().similarTo(targetType)) {
            return expr;
        }

        if (!expr.getType().isPrimitive()) {
            @Nullable Type primitiveType = expr.getType().getPrimitiveType();
            if (primitiveType == null) {
                /* Try widening reference conversion. */
                @Nullable Expression cast = tryWideningReferenceConversion(compilationContext, expr, targetType);
                if (cast != null) {
                    return cast;
                } else {
                    throw new BrygJitException("Widening reference conversion not possible!", expr.getLine());
                }
            } else {
                expr = new UnboxingExpression(compilationContext, expr, primitiveType);
            }
        }

        @Nullable Type wrapperType = null;
        @Nullable Type conversionTargetType = null;
        if (targetType.isWrapperType()) {
            wrapperType = targetType;
            conversionTargetType = targetType.getPrimitiveType();
        } else if (targetType.similarTo(Object.class)) {
            /* In this case, the value needs to be boxed and then promoted to Object. */
            wrapperType = expr.getType().getWrapperType();
        }

        if (conversionTargetType != null) {
            int conversionOpcode = getUnaryConversionOpcode(expr, conversionTargetType);
            if (conversionOpcode == NOP - 1) {
                throw new BrygJitException("Conversion from " + expr.getType() + " to " + conversionOpcode + " is not possible.", expr.getLine());
            }

        /* Convert if needed. */
            if (conversionOpcode != NOP) {
                expr = new CastExpression(compilationContext, conversionTargetType, expr, conversionOpcode, expr.getLine());
            }
        }

        /* Box if needed. */
        if (wrapperType != null) {
            expr = BoxingUtil.createBoxingExpression(compilationContext, expr);

            /* Possibly convert to Object. */
            @Nullable Expression cast = tryWideningReferenceConversion(compilationContext, expr, targetType);
            if (cast == null) {
                throw new BrygJitException("Widening reference conversion from " + expr.getType() + " to "
                        + targetType + " is not possible", expr.getLine());
            }

            if (!wrapperType.similarTo(expr.getType())) {
                throw new BrygJitException("Boxed types do not match: " + wrapperType + ", "
                        + expr.getType(), expr.getLine());
            }
        }

        return expr;
    }

    private static @Nullable Expression tryWideningReferenceConversion(CompilationContext compilationContext,
                                                                       Expression expr, Type targetType) {
        if (targetType.similarTo(expr.getType())) return expr;

        if (targetType.isAssignableFrom(expr.getType())) {
            return new CastExpression(compilationContext, targetType, expr, expr.getLine());
        } else {
            return null;
        }
    }

    /**
     * You need to make sure that all types are properly unboxed.
     *
     * @return NOP - 1 when no opcode has been found. You need to check for this.
     */
    private static int getUnaryConversionOpcode(Expression expr, Type target) {
        if (expr.getType().similarTo(target)) return NOP;

        if (expr.getType().similarTo(Integer.TYPE)) {
            /* Special case: Constant expressions that return the type int and have a value in byte or short bounds
             * can be demoted to byte or short, respectively. */
            if (expr.isConstant()) {
                @Nullable Integer valueObj = ((Integer) expr.getConstantValue());
                if (valueObj == null) {
                    throw new BrygJitException("A constant integer expression returns null instead of an integer.",
                            expr.getLine());
                }

                int value = valueObj;
                if (target.similarTo(Byte.TYPE)) {
                    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                        return I2B;
                    }
                } else if (target.similarTo(Short.TYPE)) {
                    if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                        return I2S;
                    }
                }
            }
        }

        return getPromotionOpcode(expr.getType(), target);
    }

    /**
     * Returns the conversion opcode for a number of primitive conversions.
     *
     * @param line Used for error reporting.
     * @return May return NOP if the "conversion" is implicit.
     */
    public static int getConversionOpcode(Type from, Type to, int line) {
        int opcode = getDemotionOpcode(from, to);
        if (opcode == NOP - 1) {
            opcode = getPromotionOpcode(from, to);
        }

        if (opcode == NOP - 1) {
            throw new BrygJitException("Conversion from " + from + " to " + to + " is not possible.", line);
        }

        return opcode;
    }

    /**
     * @return NOP - 1 if no opcode has been found. You need to check for this.
     */
    public static int getDemotionOpcode(Type from, Type to) {
        if (from.similarTo(Short.TYPE)) {
            if (to.similarTo(Byte.TYPE)) return I2B;
        } else if (from.similarTo(Integer.TYPE)) {
            if (to.similarTo(Byte.TYPE)) return I2B;
            if (to.similarTo(Short.TYPE)) return I2S;
            if (to.similarTo(Character.TYPE)) return I2C;
        } else if (from.similarTo(Long.TYPE)) {
            if (to.similarTo(Integer.TYPE)) return L2I;
        } else if (from.similarTo(Float.TYPE)) {
            if (to.similarTo(Integer.TYPE)) return F2I;
            if (to.similarTo(Long.TYPE)) return F2L;
        } else if (from.similarTo(Double.TYPE)) {
            if (to.similarTo(Integer.TYPE)) return D2I;
            if (to.similarTo(Long.TYPE)) return D2L;
            if (to.similarTo(Float.TYPE)) return D2F;
        }

        return NOP - 1;
    }

    /**
     * @return NOP - 1 if no opcode has been found. You need to check for this.
     */
    public static int getPromotionOpcode(Type from, Type to) {
        if (from.similarTo(Byte.TYPE)) {
            if (to.similarTo(Byte.TYPE)) return NOP;
            if (to.similarTo(Short.TYPE)) return NOP;
            if (to.similarTo(Integer.TYPE)) return NOP;
            if (to.similarTo(Long.TYPE)) return I2L;
            if (to.similarTo(Float.TYPE)) return I2F;
            if (to.similarTo(Double.TYPE)) return I2D;
            if (to.similarTo(Character.TYPE)) return I2C;
        } else if (from.similarTo(Short.TYPE)) {
            if (to.similarTo(Short.TYPE)) return NOP;
            if (to.similarTo(Integer.TYPE)) return NOP;
            if (to.similarTo(Long.TYPE)) return I2L;
            if (to.similarTo(Float.TYPE)) return I2F;
            if (to.similarTo(Double.TYPE)) return I2D;
            if (to.similarTo(Character.TYPE)) return I2C;
        } else if (from.similarTo(Integer.TYPE)) {
            if (to.similarTo(Integer.TYPE)) return NOP;
            if (to.similarTo(Long.TYPE)) return I2L;
            if (to.similarTo(Float.TYPE)) return I2F;
            if (to.similarTo(Double.TYPE)) return I2D;
            if (to.similarTo(Character.TYPE)) return I2C;
        } else if (from.similarTo(Long.TYPE)) {
            if (to.similarTo(Long.TYPE)) return NOP;
            if (to.similarTo(Float.TYPE)) return L2F;
            if (to.similarTo(Double.TYPE)) return L2D;
        } else if (from.similarTo(Float.TYPE)) {
            if (to.similarTo(Float.TYPE)) return NOP;
            if (to.similarTo(Double.TYPE)) return F2D;
        } else if (from.similarTo(Double.TYPE)) {
            if (to.similarTo(Double.TYPE)) return NOP;
        }

        return NOP - 1;
    }

}
