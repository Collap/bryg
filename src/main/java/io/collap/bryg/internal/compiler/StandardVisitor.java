package io.collap.bryg.internal.compiler;

import io.collap.bryg.internal.CompiledVariable;
import io.collap.bryg.internal.MemberFunctionCallInfo;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.ast.*;
import io.collap.bryg.internal.compiler.ast.EachStatement;
import io.collap.bryg.internal.compiler.ast.IfStatement;
import io.collap.bryg.internal.compiler.ast.WhileStatement;
import io.collap.bryg.internal.compiler.ast.expression.*;
import io.collap.bryg.internal.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.internal.compiler.ast.expression.bitwise.BitwiseNotExpression;
import io.collap.bryg.internal.compiler.ast.expression.bool.*;
import io.collap.bryg.internal.compiler.ast.expression.literal.*;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinaryShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.internal.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.internal.compiler.ast.expression.unary.IncDecExpression;
import io.collap.bryg.internal.compiler.ast.expression.unary.NegationExpression;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.compiler.util.InterpolationUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.compiler.util.ParseUtil;
import io.collap.bryg.module.Member;
import io.collap.bryg.module.MemberFunction;
import io.collap.bryg.module.MemberVariable;
import io.collap.bryg.module.Module;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class StandardVisitor extends BrygParserBaseVisitor<Node> {

    private CompilationContext compilationContext;

    public void setCompilationContext(CompilationContext compilationContext) {
        this.compilationContext = compilationContext;
    }


    //
    //  General
    //

    @Override
    public Node visit(@NotNull ParseTree tree) {
        Node node = super.visit(tree);
        if (node instanceof Expression) {
            Expression expression = (Expression) node;

            /* Wrap appropriate expressions in a ExpressionBooleanExpression.*/
            if ((expression.getType().similarTo(Boolean.TYPE) || expression.getType().similarTo(Boolean.class))
                    && !(expression instanceof BooleanExpression)) {
                node = new WrapperBooleanExpression(compilationContext, expression);
            }
        }
        return node;
    }

    @Override
    public RootNode visitStart(@NotNull BrygParser.StartContext ctx) {
        return new RootNode(compilationContext, ctx);
    }

    @Override
    public Node visitClosure(@NotNull BrygParser.ClosureContext ctx) {
        return new ClosureDeclarationNode(compilationContext, ctx);
    }

    @Override
    public StatementNode visitStatement(@NotNull BrygParser.StatementContext ctx) {
        return new StatementNode(compilationContext, ctx);
    }

    @Override
    public BlockNode visitBlock(@NotNull BrygParser.BlockContext ctx) {
        return new BlockNode(compilationContext, ctx);
    }


    //
    //  General Statement
    //

    @Override
    public VariableDeclarationNode visitVariableDeclaration(@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode(compilationContext, ctx);
    }


    //
    //  Function Call
    //

    private MemberFunctionCallExpression createMemberFunctionCallNode(BrygParser.IdContext id, int line,
                                                                      BrygParser.ArgumentListContext argumentListContext,
                                                                      @Nullable Node statementOrBlock) {
        return createMemberFunctionCallNode(IdUtil.idToString(id), null, line,
                FunctionUtil.parseArgumentList(compilationContext, argumentListContext),
                statementOrBlock
        );
    }

    private MemberFunctionCallExpression createMemberFunctionCallNode(String name, @Nullable Module module,
                                                                      int line, List<ArgumentExpression> arguments,
                                                                      @Nullable Node statementOrBlock) {
        return new MemberFunctionCallExpression(compilationContext, line, findMemberFunction(name, module, line),
                new MemberFunctionCallInfo(arguments, statementOrBlock));
    }

    /**
     * If 'module' is null, the function is searched for in the global namespace.
     */
    private MemberFunction findMemberFunction(String name, @Nullable Module module, int line) {
        @Nullable Member<?> member;
        if (module != null) {
            member = module.getMember(name);
        } else {
            member = compilationContext.getEnvironment().getGlobalMember(name);
        }

        if (member == null) {
            throw new BrygJitException("Module function " + name + " not found.", line);
        }

        if (!(member instanceof MemberFunction)) {
            throw new BrygJitException("The module member " + name + " is not a function", line);
        }

        return (MemberFunction) member;
    }

    @Override
    public MemberFunctionCallExpression visitFunctionCall(@NotNull BrygParser.FunctionCallContext ctx) {
        return createMemberFunctionCallNode(ctx.id(), ctx.getStart().getLine(),
                ctx.argumentList(), null);
    }

    @Override
    public MemberFunctionCallExpression visitBlockFunctionCall(@NotNull BrygParser.BlockFunctionCallContext ctx) {
        return createMemberFunctionCallNode(ctx.id(), ctx.getStart().getLine(),
                ctx.argumentList(), visitBlock(ctx.block()));
    }

    @Override
    public MemberFunctionCallExpression visitStatementFunctionCall(@NotNull BrygParser.StatementFunctionCallContext ctx) {
        return createMemberFunctionCallNode(ctx.id(), ctx.getStart().getLine(),
                ctx.argumentList(), visitStatement(ctx.statement()));
    }

    @Override
    public MethodCallExpression visitMethodCallExpression(@NotNull BrygParser.MethodCallExpressionContext ctx) {
        return new MethodCallExpression(compilationContext, ctx);
    }


    //
    //  Control Flow
    //

    @Override
    public IfStatement visitIfStatement(@NotNull BrygParser.IfStatementContext ctx) {
        return new IfStatement(compilationContext, ctx);
    }

    @Override
    public EachStatement visitEachStatement(@NotNull BrygParser.EachStatementContext ctx) {
        return new EachStatement(compilationContext, ctx);
    }

    @Override
    public WhileStatement visitWhileStatement(@NotNull BrygParser.WhileStatementContext ctx) {
        return new WhileStatement(compilationContext, ctx);
    }


    //
    //  Arithmetic
    //

    @Override
    public BinaryArithmeticExpression visitBinaryAddSubExpression(@NotNull BrygParser.BinaryAddSubExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression(0);
        BrygParser.ExpressionContext right = ctx.expression(1);

        if (ctx.op.getType() == BrygLexer.PLUS) {
            return new BinaryAdditionExpression(compilationContext, left, right);
        } else { /* MINUS */
            return new BinarySubtractionExpression(compilationContext, left, right);
        }
    }

    @Override
    public Node visitBinaryMulDivRemExpression(@NotNull BrygParser.BinaryMulDivRemExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression(0);
        BrygParser.ExpressionContext right = ctx.expression(1);

        int op = ctx.op.getType();
        if (op == BrygLexer.MUL) {
            return new BinaryMultiplicationExpression(compilationContext, left, right);
        } else if (op == BrygLexer.DIV) {
            return new BinaryDivisionExpression(compilationContext, left, right);
        } else { /* REM */
            return new BinaryRemainderExpression(compilationContext, left, right);
        }
    }


    //
    //  Relational
    //

    @Override
    public EqualityBinaryBooleanExpression visitBinaryEqualityExpression(@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        return new EqualityBinaryBooleanExpression(compilationContext, ctx);
    }

    @Override
    public RelationalBinaryBooleanExpression visitBinaryRelationalExpression(@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        return new RelationalBinaryBooleanExpression(compilationContext, ctx);
    }

    @Override
    public LogicalAndBinaryBooleanExpression visitBinaryLogicalAndExpression(@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        return new LogicalAndBinaryBooleanExpression(compilationContext, ctx);
    }

    @Override
    public LogicalOrBinaryBooleanExpression visitBinaryLogicalOrExpression(@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        return new LogicalOrBinaryBooleanExpression(compilationContext, ctx);
    }

    @Override
    public Node visitBinaryReferenceEqualityExpression(@NotNull BrygParser.BinaryReferenceEqualityExpressionContext ctx) {
        return new ReferenceEqualityExpression(compilationContext, ctx);
    }


    //
    //  Unary
    //

    @Override
    public @Nullable Expression visitVariable(@NotNull BrygParser.VariableContext ctx) {
        int line = ctx.getStart().getLine();
        String id = IdUtil.idToString(ctx.id());
        @Nullable CompiledVariable variable = compilationContext.getCurrentScope().getVariable(id);

        if (variable != null) {
            return new VariableExpression(compilationContext, ctx.getStart().getLine(), variable,
                    VariableUsageInfo.withGetMode());
        } else { // The variable is probably a member of a module.
            @Nullable Member<?> member = compilationContext.getEnvironment().getGlobalMember(id);

            if (member != null) {
                // TODO: Is instanceof ugly here?
                if (member instanceof MemberFunction) {
                    return createMemberFunctionCallNode(id, null, line, new ArrayList<>(), null);
                } else if (member instanceof MemberVariable<?>) {
                    return new VariableExpression(compilationContext, line, (MemberVariable<?>) member,
                            VariableUsageInfo.withGetMode());
                } else {
                    throw new BrygJitException("Member " + id + " is neither a function nor a variable. Unknown type.", line);
                }
            }
        }

        throw new BrygJitException("Variable " + id + " not found.", line);
    }

    @Override
    public @Nullable Expression visitExpressionPrecedenceOrder(@NotNull BrygParser.ExpressionPrecedenceOrderContext ctx) {
        return (Expression) visit(ctx.expression());
    }

    @Override
    public AccessExpression visitAccessExpression(@NotNull BrygParser.AccessExpressionContext ctx) {
        try {
            /* Note: This corresponds to the getter only, the setter scenario is handled by the assignment expression! */
            return new AccessExpression(compilationContext, ctx, AccessMode.get);
        } catch (NoSuchFieldException e) {
            throw new BrygJitException("Field not found.", ctx.getStart().getLine());
        }
    }

    @Override
    public CastExpression visitCastExpression(@NotNull BrygParser.CastExpressionContext ctx) {
        return new CastExpression(compilationContext, ctx);
    }

    @Override
    public IncDecExpression visitUnaryPostfixExpression(@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
        final int op = ctx.op.getType();
        final int line = ctx.getStart().getLine();
        return new IncDecExpression(compilationContext, ctx.expression(), op == BrygLexer.INC, false, line);
    }

    @Override
    public Node visitUnaryPrefixExpression(@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        final int op = ctx.op.getType();
        final int line = ctx.getStart().getLine();
        if (op == BrygLexer.MINUS) {
            return new NegationExpression(compilationContext, (Expression) visit(ctx.expression()), line);
        } else {
            return new IncDecExpression(compilationContext, ctx.expression(), op == BrygLexer.INC, true, line);
        }
    }

    @Override
    public Expression visitUnaryOperationExpression(@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        final int op = ctx.op.getType();
        if (op == BrygLexer.BNOT) {
            return new BitwiseNotExpression(compilationContext, ctx.expression());
        } else { /* NOT */
            return new LogicalNotBooleanExpression(compilationContext, ctx.expression());
        }
    }


    //
    //  Bitwise
    //

    @Override
    public BinaryBitwiseAndExpression visitBinaryBitwiseAndExpression(@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        return new BinaryBitwiseAndExpression(compilationContext, ctx);
    }

    @Override
    public BinaryBitwiseXorExpression visitBinaryBitwiseXorExpression(@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        return new BinaryBitwiseXorExpression(compilationContext, ctx);
    }

    @Override
    public BinaryBitwiseOrExpression visitBinaryBitwiseOrExpression(@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        return new BinaryBitwiseOrExpression(compilationContext, ctx);
    }


    //
    //  Shift
    //

    @Override
    public BinaryShiftExpression visitBinaryShiftExpression(@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        int op = ctx.op.getType();
        if (op == BrygLexer.SIG_LSHIFT) {
            return new BinarySignedLeftShiftExpression(compilationContext, ctx);
        } else if (op == BrygLexer.SIG_RSHIFT) {
            return new BinarySignedRightShiftExpression(compilationContext, ctx);
        } else { /* UNSIG_RSHIFT */
            return new BinaryUnsignedRightShiftExpression(compilationContext, ctx);
        }
    }


    //
    //  Assignment
    //

    @Override
    public Node visitBinaryAssignmentExpression(@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression(compilationContext, ctx);
    }


    //
    //  Literal
    //
    //      Note: The parser ensures that no NumberFormatException are thrown here.
    //

    @Override
    public ValueLiteralExpression<Integer> visitIntegerLiteral(@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new ValueLiteralExpression<>(compilationContext, ctx.getStart().getLine(),
                Integer.TYPE, Integer.parseInt(ctx.Integer().getText()));
    }

    @Override
    public ValueLiteralExpression<Long> visitLongLiteral(@NotNull BrygParser.LongLiteralContext ctx) {
        return new ValueLiteralExpression<>(compilationContext, ctx.getStart().getLine(),
                Long.TYPE, ParseUtil.parseLong(ctx.Long().getText()));
    }

    @Override
    public ValueLiteralExpression<Double> visitDoubleLiteral(@NotNull BrygParser.DoubleLiteralContext ctx) {
        return new ValueLiteralExpression<>(compilationContext, ctx.getStart().getLine(),
                Double.TYPE, Double.parseDouble(ctx.Double().getText()));
    }

    @Override
    public ValueLiteralExpression<Float> visitFloatLiteral(@NotNull BrygParser.FloatLiteralContext ctx) {
        return new ValueLiteralExpression<>(compilationContext, ctx.getStart().getLine(),
                Float.TYPE, ParseUtil.parseFloat(ctx.Float().getText()));
    }

    /**
     * Returns either a ValueLiteralExpression&lt;String&gt; or a InterpolationExpression.
     */
    @Override
    public Expression visitStringLiteral(@NotNull BrygParser.StringLiteralContext ctx) {
        String text = ctx.getText();
        int line = ctx.getStart().getLine();
        return InterpolationUtil.compileString(compilationContext, text, line);
    }

    @Override
    public NullLiteralExpression visitNullLiteral(@NotNull BrygParser.NullLiteralContext ctx) {
        return new NullLiteralExpression(compilationContext, ctx.getStart().getLine());
    }

    @Override
    public BooleanLiteralExpression visitBooleanLiteral(@NotNull BrygParser.BooleanLiteralContext ctx) {
        return new BooleanLiteralExpression(compilationContext, ctx.getStart().getLine(),
                ctx.value.getType() == BrygLexer.TRUE);
    }


    //
    //  Interpolation
    //

    @Override
    public Expression visitInterpolation(@NotNull BrygParser.InterpolationContext ctx) {
        return (Expression) visit(ctx.expression());
    }


    //
    //  Currently Unsupported
    //

    @Override
    public Node visitBinaryIsExpression(@NotNull BrygParser.BinaryIsExpressionContext ctx) {
        throw new UnsupportedOperationException("The 'is' operator is not yet implemented.");
    }

}
