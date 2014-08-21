package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.expression.Operator;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

import static org.objectweb.asm.Opcodes.*;

public abstract class OperatorBinaryBooleanExpression extends BinaryBooleanExpression {

    protected Operator operator;

    protected OperatorBinaryBooleanExpression (StandardVisitor visitor, BrygParser.ExpressionContext left,
                                               BrygParser.ExpressionContext right, Operator operator) {
        super (visitor, left, right);
        this.operator = operator;
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        if (left == null || right == null) {
            throw new BrygJitException ("Left or right is null: " + left + ", " + right, getLine ());
        }

        BrygMethodVisitor method = visitor.getMethod ();

        Type operandType = CoercionHelper.getTargetType (left.getType (), right.getType (), getLine ());
        CoercionHelper.attemptBinaryCoercion (method, left, right, operandType);

        // -> T, T

        if (operandType.getJavaType ().isPrimitive ()) {
            if (operandType.equals (Integer.TYPE)) {
                switch (operator) {
                    case equality:
                        method.visitJumpInsn (IF_ICMPNE, nextFalse);
                        break;
                    case inequality:
                        method.visitJumpInsn (IF_ICMPEQ, nextFalse);
                        break;

                    /* The relational tests have to test the opposite for a "jump when false" scenario. */
                    case relational_greater_than:
                        method.visitJumpInsn (IF_ICMPLE, nextFalse);
                        break;
                    case relational_greater_equal:
                        method.visitJumpInsn (IF_ICMPLT, nextFalse);
                        break;
                    case relational_less_than:
                        method.visitJumpInsn (IF_ICMPGE, nextFalse);
                        break;
                    case relational_less_equal:
                        method.visitJumpInsn (IF_ICMPGT, nextFalse);
                        break;

                    default:
                        throw new BrygJitException ("Unexpected boolean operator!", getLine ());
                }
            }else {
                if (operandType.equals (Double.TYPE)) {
                    method.visitInsn (DCMPG);
                    // d1, d2 -> int
                }else if (operandType.equals (Float.TYPE)) {
                    method.visitInsn (FCMPG);
                    // f1, f2 -> int
                }else if (operandType.equals (Long.TYPE)) {
                    method.visitInsn (LCMP);
                    // l1, l2 -> int
                }else {
                    throw new BrygJitException ("Unknown operand type " + operandType + " for relational operation.",
                        getLine ());
                }

                switch (operator) {
                    // dcmpg/fcmpg returns 0 if equal.
                    case equality:
                        method.visitJumpInsn (IFNE, nextFalse);
                        break;
                    case inequality:
                        method.visitJumpInsn (IFEQ, nextFalse);
                        break;

                    // dcmpg/fcmpg returns -1 if d1 > d2, 1 if d1 < d2.
                    case relational_greater_than:
                        method.visitJumpInsn (IFLE, nextFalse);
                        break;
                    case relational_greater_equal:
                        method.visitJumpInsn (IFLT, nextFalse);
                        break;
                    case relational_less_than:
                        method.visitJumpInsn (IFGE, nextFalse);
                        break;
                    case relational_less_equal:
                        method.visitJumpInsn (IFGT, nextFalse);
                        break;

                    default:
                        throw new BrygJitException ("Unexpected boolean operator!", getLine ());
                }
            }
        }else { /* Objects. */
            // TODO: With the coercion model above, the types have to be exactly the same!
            method.visitMethodInsn (INVOKEVIRTUAL, AsmTypes.getAsmType (Object.class).getInternalName (),
                    "equals", TypeHelper.generateMethodDesc (
                            new Class<?>[]{Object.class},
                            Boolean.TYPE
                    ), false);
            // Object, Object -> boolean

            switch (operator) {
                case equality:
                    method.visitJumpInsn (IFEQ, nextFalse); /* equality is false when the result equals 0 (false). */
                    break;
                case inequality:
                    method.visitJumpInsn (IFNE, nextFalse); /* inequality is false when the result does not equal 0 (true). */
                    break;
                default:
                    throw new BrygJitException ("Unexpected boolean operator!", getLine ());
            }
        }

        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
