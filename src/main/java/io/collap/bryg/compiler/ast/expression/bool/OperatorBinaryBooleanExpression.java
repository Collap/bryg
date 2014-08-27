package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Label;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;

public abstract class OperatorBinaryBooleanExpression extends BinaryBooleanExpression {

    protected int operator;

    protected OperatorBinaryBooleanExpression (Context context, BrygParser.ExpressionContext left,
                                               BrygParser.ExpressionContext right, int operator) {
        super (context, left, right);
        this.operator = operator;
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        if (left == null || right == null) {
            throw new BrygJitException ("Left or right is null: " + left + ", " + right, getLine ());
        }

        BrygMethodVisitor mv = context.getMethodVisitor ();

        Type operandType = CoercionUtil.getTargetType (left.getType (), right.getType (), getLine ());
        CoercionUtil.attemptBinaryCoercion (mv, left, right, operandType);

        // -> T, T

        if (operandType.getJavaType ().isPrimitive ()) {
            if (operandType.equals (Integer.TYPE)) {
                switch (operator) {
                    case BrygLexer.REQ:
                        mv.visitJumpInsn (IF_ICMPNE, nextFalse);
                        break;
                    case BrygLexer.RNE:
                        mv.visitJumpInsn (IF_ICMPEQ, nextFalse);
                        break;

                    /* The relational tests have to test the opposite for a "jump when false" scenario. */
                    case BrygLexer.RGT:
                        mv.visitJumpInsn (IF_ICMPLE, nextFalse);
                        break;
                    case BrygLexer.RGE:
                        mv.visitJumpInsn (IF_ICMPLT, nextFalse);
                        break;
                    case BrygLexer.RLT:
                        mv.visitJumpInsn (IF_ICMPGE, nextFalse);
                        break;
                    case BrygLexer.RLE:
                        mv.visitJumpInsn (IF_ICMPGT, nextFalse);
                        break;

                    default:
                        throw new BrygJitException ("Unexpected boolean operator!", getLine ());
                }
            }else {
                if (operandType.equals (Double.TYPE)) {
                    mv.visitInsn (DCMPG);
                    // d1, d2 -> int
                }else if (operandType.equals (Float.TYPE)) {
                    mv.visitInsn (FCMPG);
                    // f1, f2 -> int
                }else if (operandType.equals (Long.TYPE)) {
                    mv.visitInsn (LCMP);
                    // l1, l2 -> int
                }else {
                    throw new BrygJitException ("Unknown operand type " + operandType + " for relational operation.",
                        getLine ());
                }

                switch (operator) {
                    // dcmpg/fcmpg returns 0 if equal.
                    case BrygLexer.REQ:
                        mv.visitJumpInsn (IFNE, nextFalse);
                        break;
                    case BrygLexer.RNE:
                        mv.visitJumpInsn (IFEQ, nextFalse);
                        break;

                    // dcmpg/fcmpg returns -1 if d1 > d2, 1 if d1 < d2.
                    case BrygLexer.RGT:
                        mv.visitJumpInsn (IFLE, nextFalse);
                        break;
                    case BrygLexer.RGE:
                        mv.visitJumpInsn (IFLT, nextFalse);
                        break;
                    case BrygLexer.RLT:
                        mv.visitJumpInsn (IFGE, nextFalse);
                        break;
                    case BrygLexer.RLE:
                        mv.visitJumpInsn (IFGT, nextFalse);
                        break;

                    default:
                        throw new BrygJitException ("Unexpected boolean operator!", getLine ());
                }
            }
        }else { /* Objects. */
            // TODO: With the coercion model above, the object types have to be exactly the same!
            mv.visitMethodInsn (INVOKEVIRTUAL, AsmTypes.getAsmType (Object.class).getInternalName (),
                    "equals", TypeHelper.generateMethodDesc (
                            new Class<?>[]{Object.class},
                            Boolean.TYPE
                    ), false);
            // Object, Object -> boolean

            switch (operator) {
                case BrygLexer.REQ:
                    mv.visitJumpInsn (IFEQ, nextFalse); /* equality is false when the result equals 0 (false). */
                    break;
                case BrygLexer.RNE:
                    mv.visitJumpInsn (IFNE, nextFalse); /* inequality is false when the result does not equal 0 (true). */
                    break;
                default:
                    throw new BrygJitException ("Unexpected boolean operator!", getLine ());
            }
        }

        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
