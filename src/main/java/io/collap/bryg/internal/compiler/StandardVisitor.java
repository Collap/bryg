package io.collap.bryg.internal.compiler;

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
import io.collap.bryg.module.Function;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.compiler.util.InterpolationUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nullable;

public class StandardVisitor extends BrygParserBaseVisitor<Node> {

    private CompilationContext compilationContext;

    public void setCompilationContext(CompilationContext compilationContext) {
        this.compilationContext = compilationContext;
    }


    //
    //  General
    //

    @Override
    public Node visit (@NotNull ParseTree tree) {
        Node node = super.visit (tree);
        if (node instanceof Expression) {
            Expression expression = (Expression) node;

            /* Wrap appropriate expressions in a ExpressionBooleanExpression.*/
            if ((expression.getType ().similarTo (Boolean.TYPE) || expression.getType ().similarTo (Boolean.class))
                    && !(expression instanceof BooleanExpression)) {
                node = new ExpressionBooleanExpression (compilationContext, expression);
            }
        }
        return node;
    }

    @Override
    public RootNode visitStart (@NotNull BrygParser.StartContext ctx) {
        return new RootNode (compilationContext, ctx);
    }

    @Override
    public Node visitClosure (@NotNull BrygParser.ClosureContext ctx) {
        return new RootNode (compilationContext, ctx.statement ());
    }

    @Override
    public Node visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
        throw new BrygJitException ("In declarations can not be compiled!", ctx.getStart ().getLine ());
    }

    @Override
    public StatementNode visitStatement (@NotNull BrygParser.StatementContext ctx) {
        return new StatementNode (compilationContext, ctx);
    }

    @Override
    public BlockNode visitBlock (@NotNull BrygParser.BlockContext ctx) {
        return new BlockNode (compilationContext, ctx);
    }


    //
    //  General Statement
    //

    @Override
    public VariableDeclarationNode visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode (compilationContext, ctx);
    }


    //
    //  Function Call
    //

    @Override
    public FunctionCallExpression visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        return new FunctionCallExpression (compilationContext, ctx);
    }

    @Override
    public FunctionCallExpression visitBlockFunctionCall (@NotNull BrygParser.BlockFunctionCallContext ctx) {
        return new FunctionCallExpression (compilationContext, ctx);
    }

    @Override
    public FunctionCallExpression visitStatementFunctionCall (@NotNull BrygParser.StatementFunctionCallContext ctx) {
        return new FunctionCallExpression (compilationContext, ctx);
    }

    @Override
    public MethodCallExpression visitMethodCallExpression (@NotNull BrygParser.MethodCallExpressionContext ctx) {
        return new MethodCallExpression (compilationContext, ctx);
    }

    @Override
    public TemplateFragmentCall visitTemplateFragmentCall (@NotNull BrygParser.TemplateFragmentCallContext ctx) {
        return new TemplateFragmentCall (compilationContext, ctx);
    }


    //
    //  Control Flow
    //

    @Override
    public IfStatement visitIfStatement (@NotNull BrygParser.IfStatementContext ctx) {
        return new IfStatement (compilationContext, ctx);
    }

    @Override
    public EachStatement visitEachStatement (@NotNull BrygParser.EachStatementContext ctx) {
        return new EachStatement (compilationContext, ctx);
    }

    @Override
    public WhileStatement visitWhileStatement (@NotNull BrygParser.WhileStatementContext ctx) {
        return new WhileStatement (compilationContext, ctx);
    }


    //
    //  Arithmetic
    //

    @Override
    public BinaryArithmeticExpression visitBinaryAddSubExpression (@NotNull BrygParser.BinaryAddSubExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        if (ctx.op.getType () == BrygLexer.PLUS) {
            return new BinaryAdditionExpression (compilationContext, left, right);
        }else { /* MINUS */
            return new BinarySubtractionExpression (compilationContext, left, right);
        }
    }

    @Override
    public Node visitBinaryMulDivRemExpression (@NotNull BrygParser.BinaryMulDivRemExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        int op = ctx.op.getType ();
        if (op == BrygLexer.MUL) {
            return new BinaryMultiplicationExpression (compilationContext, left, right);
        }else if (op == BrygLexer.DIV) {
            return new BinaryDivisionExpression (compilationContext, left, right);
        }else { /* REM */
            return new BinaryRemainderExpression (compilationContext, left, right);
        }
    }


    //
    //  Relational
    //

    @Override
    public EqualityBinaryBooleanExpression visitBinaryEqualityExpression (@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        return new EqualityBinaryBooleanExpression (compilationContext, ctx);
    }

    @Override
    public RelationalBinaryBooleanExpression visitBinaryRelationalExpression (@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        return new RelationalBinaryBooleanExpression (compilationContext, ctx);
    }

    @Override
    public LogicalAndBinaryBooleanExpression visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        return new LogicalAndBinaryBooleanExpression (compilationContext, ctx);
    }

    @Override
    public LogicalOrBinaryBooleanExpression visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        return new LogicalOrBinaryBooleanExpression (compilationContext, ctx);
    }

    @Override
    public Node visitBinaryReferenceEqualityExpression (@NotNull BrygParser.BinaryReferenceEqualityExpressionContext ctx) {
        return new ReferenceEqualityExpression (compilationContext, ctx);
    }


    //
    //  Unary
    //

    @Override
    public @Nullable Expression visitVariable (@NotNull BrygParser.VariableContext ctx) {
        String id = IdUtil.idToString (ctx.id ());
        if (id != null) {
            Variable variable = compilationContext.getCurrentScope ().getVariable (id);

            if (variable != null) {
                return new VariableExpression (compilationContext, ctx.getStart ().getLine (), variable, AccessMode.get);
            }else { /* The variable is probably a function. */
                Function function = compilationContext.getEnvironment ().getLibrary ().getFunction (id);
                if (function != null) {
                    return new FunctionCallExpression (compilationContext, function, ctx.getStart ().getLine ());
                }
            }

            throw new BrygJitException ("Variable " + id + " not found.", ctx.getStart ().getLine ());
        }

        return null;
    }

    @Override
    public @Nullable Expression visitExpressionPrecedenceOrder (@NotNull BrygParser.ExpressionPrecedenceOrderContext ctx) {
        return (Expression) visit (ctx.expression ());
    }

    @Override
    public AccessExpression visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        try {
            /* Note: This corresponds to the getter only, the setter scenario is handled by the assignment expression! */
            return new AccessExpression (compilationContext, ctx, AccessMode.get);
        } catch (NoSuchFieldException e) {
            e.printStackTrace ();
            return null;
        }
    }

    @Override
    public CastExpression visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        return new CastExpression (compilationContext, ctx);
    }

    @Override
    public IncDecExpression visitUnaryPostfixExpression (@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        return new IncDecExpression (compilationContext, ctx.expression (), op == BrygLexer.INC, false, line);
    }

    @Override
    public Node visitUnaryPrefixExpression (@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        if (op == BrygLexer.MINUS) {
            return new NegationExpression (compilationContext, (Expression) visit (ctx.expression ()), line);
        }else {
            return new IncDecExpression (compilationContext, ctx.expression (), op == BrygLexer.INC, true, line);
        }
    }

    @Override
    public Expression visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        final int op = ctx.op.getType ();
        if (op == BrygLexer.BNOT) {
            return new BitwiseNotExpression (compilationContext, ctx.expression ());
        }else { /* NOT */
            return new LogicalNotBooleanExpression (compilationContext, ctx.expression ());
        }
    }


    //
    //  Bitwise
    //

    @Override
    public BinaryBitwiseAndExpression visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        return new BinaryBitwiseAndExpression (compilationContext, ctx);
    }

    @Override
    public BinaryBitwiseXorExpression visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        return new BinaryBitwiseXorExpression (compilationContext, ctx);
    }

    @Override
    public BinaryBitwiseOrExpression visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        return new BinaryBitwiseOrExpression (compilationContext, ctx);
    }


    //
    //  Shift
    //

    @Override
    public BinaryShiftExpression visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        int op = ctx.op.getType ();
        if (op == BrygLexer.SIG_LSHIFT) {
            return new BinarySignedLeftShiftExpression (compilationContext, ctx);
        }else if (op == BrygLexer.SIG_RSHIFT) {
            return new BinarySignedRightShiftExpression (compilationContext, ctx);
        }else { /* UNSIG_RSHIFT */
            return new BinaryUnsignedRightShiftExpression (compilationContext, ctx);
        }
    }


    //
    //  Assignment
    //

    @Override
    public Node visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression (compilationContext, ctx);
    }


    //
    //  Literal
    //

    @Override
    public IntegerLiteralExpression visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new IntegerLiteralExpression (compilationContext, ctx);
    }

    @Override
    public Node visitLongLiteral (@NotNull BrygParser.LongLiteralContext ctx) {
        return new LongLiteralExpression (compilationContext, ctx);
    }

    @Override
    public DoubleLiteralExpression visitDoubleLiteral (@NotNull BrygParser.DoubleLiteralContext ctx) {
        return new DoubleLiteralExpression (compilationContext, ctx);
    }

    @Override
    public FloatLiteralExpression visitFloatLiteral (@NotNull BrygParser.FloatLiteralContext ctx) {
        return new FloatLiteralExpression (compilationContext, ctx);
    }

    @Override
    public Expression visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        String text = ctx.getText ();
        int line = ctx.getStart ().getLine ();
        return InterpolationUtil.compileString (compilationContext, text, line);
    }

    @Override
    public ObjectLiteralExpression visitNullLiteral (@NotNull BrygParser.NullLiteralContext ctx) {
        return new ObjectLiteralExpression (compilationContext, null, ctx.getStart ().getLine ());
    }

    @Override
    public Node visitBooleanLiteral (@NotNull BrygParser.BooleanLiteralContext ctx) {
        return new BooleanLiteralExpression (compilationContext, ctx);
    }


    //
    //  Interpolation
    //

    @Override
    public Expression visitInterpolation (@NotNull BrygParser.InterpolationContext ctx) {
        return (Expression) visit (ctx.expression ());
    }


    //
    //  Currently Unsupported
    //

    @Override
    public Node visitBinaryIsExpression (@NotNull BrygParser.BinaryIsExpressionContext ctx) {
        throw new UnsupportedOperationException ("The 'is' operator is not yet implemented.");
    }

}
