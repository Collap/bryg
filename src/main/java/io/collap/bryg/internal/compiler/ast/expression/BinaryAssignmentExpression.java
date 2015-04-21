package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.CompiledVariable;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class BinaryAssignmentExpression extends Expression {

    // TODO: Make the indication of casting easier for these scenarios: (int) (value / 0.5) ; Where value is an int. (Maybe fix)

    private Node assignmentNode;

    public BinaryAssignmentExpression(CompilationContext compilationContext, BrygParser.BinaryAssignmentExpressionContext ctx) {
        super(compilationContext, ctx.getStart().getLine());
        setType(Types.fromClass(Void.TYPE)); // TODO: Implement as plain Node?

        BrygParser.ExpressionContext leftCtx = ctx.expression(0);
        int operator = ctx.op.getType();

        // For variables
        @Nullable CompiledVariable variable;

        // For accessed objects
        final @Nullable Node initObject; // Used to initialize the accessedObject, so that the left expression is not evaluated twice.
        @Nullable Expression accessedObject;
        @Nullable Field field;

        Type expectedType;
        if (leftCtx instanceof BrygParser.VariableExpressionContext) {
            initObject = null;
            accessedObject = null;
            field = null;

            BrygParser.VariableExpressionContext variableCtx = (BrygParser.VariableExpressionContext) leftCtx;
            String variableName = IdUtil.idToString(variableCtx.variable().id());
            int variableLine = ctx.getStart().getLine();

            variable = compilationContext.getCurrentScope().getVariable(variableName);
            if (variable == null) {
                throw new BrygJitException("Variable '" + variableName + "' not found.", variableLine);
            }

            if (variable.getMutability() == Mutability.immutable) {
                throw new BrygJitException("Variable '" + variableName + "' is not mutable.", variableLine);
            }

            expectedType = variable.getType();
        } else if (leftCtx instanceof BrygParser.AccessExpressionContext) {
            variable = null;

            BrygParser.AccessExpressionContext accessCtx = (BrygParser.AccessExpressionContext) leftCtx;
            accessedObject = (Expression) compilationContext.getParseTreeVisitor().visit(accessCtx.expression());

            // Compile the accessed object only once. Only needs to be set when there
            // is a potential to be compiled twice, hence when the operator is of the form
            // a op= b <=> a = a op b.
            if (operator != BrygLexer.ASSIGN && !(accessedObject instanceof VariableExpression)) {
                CompiledVariable tmp = compilationContext.getCurrentScope().createTemporalVariable(
                        accessedObject.getType(), Mutability.immutable, Nullness.nullable
                );
                initObject = new VariableExpression(compilationContext, getLine(), tmp,
                        VariableUsageInfo.withSetMode(accessedObject));
                accessedObject = new VariableExpression(compilationContext, getLine(), tmp,
                        VariableUsageInfo.withGetMode());
            } else {
                initObject = null;
            }

            String fieldName = IdUtil.idToString(accessCtx.id());
            try {
                field = TypeHelper.findField(accessedObject.getType(), getLine(), fieldName);
            } catch (NoSuchFieldException e) {
                throw new BrygJitException("Field " + fieldName + " not found!", getLine(), e);
            }
            expectedType = Types.fromClass(field.getType());
        } else {
            throw new BrygJitException("The assignment expression does not include an assignable left hand expression!",
                    getLine());
        }

        // Create right hand side expression node.
        Expression right = (Expression) compilationContext.getParseTreeVisitor().visit(ctx.expression(1));

        // Handle +=, -=, etc. cases.
        if (operator != BrygLexer.ASSIGN) {
            // Create leftGet expression node, which is used in a = a op b expressions.
            // Side effects have been ruled out by the setup before this code block is reached.
            Expression leftGet;
            if (variable != null) { // Variable.
                leftGet = new VariableExpression(compilationContext, getLine(),
                        variable, VariableUsageInfo.withGetMode());
            } else { // Object access.
                leftGet = new AccessExpression(compilationContext, getLine(),
                        accessedObject, field, AccessMode.get, null);
            }

            switch (operator) {
                case BrygLexer.ADD_ASSIGN:
                    right = new BinaryAdditionExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.SUB_ASSIGN:
                    right = new BinarySubtractionExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.MUL_ASSIGN:
                    right = new BinaryMultiplicationExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.DIV_ASSIGN:
                    right = new BinaryDivisionExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.REM_ASSIGN:
                    right = new BinaryRemainderExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.BAND_ASSIGN:
                    right = new BinaryBitwiseAndExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.BXOR_ASSIGN:
                    right = new BinaryBitwiseXorExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.BOR_ASSIGN:
                    right = new BinaryBitwiseOrExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.SIG_LSHIFT_ASSIGN:
                    right = new BinarySignedLeftShiftExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.SIG_RSHIFT_ASSIGN:
                    right = new BinarySignedRightShiftExpression(compilationContext, leftGet, right, getLine());
                    break;
                case BrygLexer.UNSIG_RSHIFT_ASSIGN:
                    right = new BinaryUnsignedRightShiftExpression(compilationContext, leftGet, right, getLine());
                    break;
                default:
                    throw new BrygJitException("Operator " + operator + " is not supported in assignments!", getLine());
            }
        }

        // Possible coercion.
        if (!expectedType.similarTo(right.getType())) {
            right = CoercionUtil.applyUnaryCoercion(compilationContext, right, expectedType);
        }

        // Set assignment node.
        if (variable != null) {
            assignmentNode = new VariableExpression(compilationContext, getLine(),
                    variable, VariableUsageInfo.withSetMode(right));
        } else { // Object access.
            final AccessExpression accessExpression = new AccessExpression(compilationContext, getLine(),
                    accessedObject, field, AccessMode.set, right);
            assignmentNode = new Node(compilationContext, getLine()) {
                @Override
                public void compile() {
                    if (initObject != null) {
                        initObject.compile();
                    }

                    accessExpression.compile();
                }
            };
        }
    }

    @Override
    public void compile() {
        assignmentNode.compile();
    }

}
