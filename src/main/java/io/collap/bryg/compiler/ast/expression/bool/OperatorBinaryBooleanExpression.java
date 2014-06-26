package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.expression.Operator;
import io.collap.bryg.compiler.type.Type;
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
            throw new UnsupportedOperationException ("Left or right is null: " + left + ", " + right);
        }

        if (!areBinaryExpressionTypesValid (left, right)) {
            throw new UnsupportedOperationException ("Left and right have different types!");
        }

        left.compile ();
        right.compile ();

        BrygMethodVisitor method = visitor.getMethod ();

        Type type = left.getType ();
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
                    throw new UnsupportedOperationException ("Unexpected boolean operator!");
            }
        }

        /* At this point the expression is supposed to be true. */
        if (nextTrue != null && !lastExpressionInChain) {
            method.visitJumpInsn (GOTO, nextTrue);
        }
    }

    private boolean areBinaryExpressionTypesValid (Expression left, Expression right) {
        return left.getType ().equals (right.getType ());
    }

}
