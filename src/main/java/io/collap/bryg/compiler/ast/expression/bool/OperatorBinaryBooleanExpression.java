package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
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

        if (!areBinaryExpressionTypesValid (left, right)) {
            // TODO: Attempt coercion.
            throw new BrygJitException ("Left and right have incompatible types: "
                + left.getType ().getJavaType ()
                + " " + right.getType ().getJavaType (), getLine ());
        }

        left.compile ();
        right.compile ();
        // -> T1, T2

        BrygMethodVisitor method = visitor.getMethod ();

        Type type = left.getType ();
        if (type.getJavaType ().isPrimitive ()) {
            if (type.equals (Integer.TYPE)) {
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
            }
        }else { /* Objects. */
            method.visitMethodInsn (INVOKEVIRTUAL, AsmTypes.getAsmType (Object.class).getInternalName (),
                "equals", TypeHelper.generateMethodDesc (
                    new Class<?>[] { Object.class },
                    Boolean.TYPE
                ), false);
            // Object, Object -> boolean

            switch (operator) {
                case equality:
                    method.visitJumpInsn (IFEQ, nextFalse); /* equality is false when the result equals 0. */
                    break;
                case inequality:
                    method.visitJumpInsn (IFNE, nextFalse); /* inequality is false when the result does not equal 0. */
                    break;
                default:
                    throw new BrygJitException ("Unexpected boolean operator!", getLine ());
            }
        }

        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

    private boolean areBinaryExpressionTypesValid (Expression left, Expression right) {
        if (left.getType ().getJavaType ().isPrimitive () || right.getType ().getJavaType ().isPrimitive ()) {
            return left.getType ().equals (right.getType ());
        }else {
            return true;
        }
    }

}
