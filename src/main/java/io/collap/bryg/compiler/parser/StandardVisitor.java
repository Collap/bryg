package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.ast.*;
import io.collap.bryg.compiler.ast.control.EachStatement;
import io.collap.bryg.compiler.ast.control.IfStatement;
import io.collap.bryg.compiler.ast.control.WhileStatement;
import io.collap.bryg.compiler.ast.expression.*;
import io.collap.bryg.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.compiler.ast.expression.bitwise.*;
import io.collap.bryg.compiler.ast.expression.bool.*;
import io.collap.bryg.compiler.ast.expression.literal.*;
import io.collap.bryg.compiler.ast.expression.shift.BinaryShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.ast.expression.unary.IncDecExpression;
import io.collap.bryg.compiler.ast.expression.unary.NegationExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.compiler.util.InterpolationUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;

public class StandardVisitor extends BrygParserBaseVisitor<Node> {

    private Context context;

    public void setContext (Context context) {
        this.context = context;
    }


    //
    //  General
    //

    @Override
    public RootNode visitStart (@NotNull BrygParser.StartContext ctx) {
        return new RootNode (context, ctx);
    }

    @Override
    public @Nullable InDeclarationNode visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
        try {
            return new InDeclarationNode (context, ctx);
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }
        return null;
    }

    @Override
    public StatementNode visitStatement (@NotNull BrygParser.StatementContext ctx) {
        return new StatementNode (context, ctx);
    }

    @Override
    public BlockNode visitBlock (@NotNull BrygParser.BlockContext ctx) {
        return new BlockNode (context, ctx);
    }


    //
    //  General Statement
    //

    @Override
    public VariableDeclarationNode visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode (context, ctx);
    }


    //
    //  Function Call
    //

    @Override
    public FunctionCallExpression visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        return new FunctionCallExpression (context, ctx);
    }

    @Override
    public FunctionCallExpression visitBlockFunctionCall (@NotNull BrygParser.BlockFunctionCallContext ctx) {
        return new FunctionCallExpression (context, ctx);
    }

    @Override
    public FunctionCallExpression visitStatementFunctionCall (@NotNull BrygParser.StatementFunctionCallContext ctx) {
        return new FunctionCallExpression (context, ctx);
    }

    @Override
    public MethodCallExpression visitMethodCallExpression (@NotNull BrygParser.MethodCallExpressionContext ctx) {
        return new MethodCallExpression (context, ctx);
    }

    @Override
    public TemplateFragmentCall visitTemplateFragmentCall (@NotNull BrygParser.TemplateFragmentCallContext ctx) {
        return new TemplateFragmentCall (context, ctx);
    }


    //
    //  Control Flow
    //

    @Override
    public IfStatement visitIfStatement (@NotNull BrygParser.IfStatementContext ctx) {
        return new IfStatement (context, ctx);
    }

    @Override
    public EachStatement visitEachStatement (@NotNull BrygParser.EachStatementContext ctx) {
        return new EachStatement (context, ctx);
    }

    @Override
    public WhileStatement visitWhileStatement (@NotNull BrygParser.WhileStatementContext ctx) {
        return new WhileStatement (context, ctx);
    }


    //
    //  Arithmetic
    //

    @Override
    public BinaryArithmeticExpression visitBinaryAddSubExpression (@NotNull BrygParser.BinaryAddSubExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        if (ctx.op.getType () == BrygLexer.PLUS) {
            return new BinaryAdditionExpression (context, left, right);
        }else { /* MINUS */
            return new BinarySubtractionExpression (context, left, right);
        }
    }

    @Override
    public Node visitBinaryMulDivRemExpression (@NotNull BrygParser.BinaryMulDivRemExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        int op = ctx.op.getType ();
        if (op == BrygLexer.MUL) {
            return new BinaryMultiplicationExpression (context, left, right);
        }else if (op == BrygLexer.DIV) {
            return new BinaryDivisionExpression (context, left, right);
        }else { /* REM */
            return new BinaryRemainderExpression (context, left, right);
        }
    }


    //
    //  Relational
    //

    @Override
    public EqualityBinaryBooleanExpression visitBinaryEqualityExpression (@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        return new EqualityBinaryBooleanExpression (context, ctx);
    }

    @Override
    public RelationalBinaryBooleanExpression visitBinaryRelationalExpression (@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        return new RelationalBinaryBooleanExpression (context, ctx);
    }

    @Override
    public LogicalAndBinaryBooleanExpression visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        return new LogicalAndBinaryBooleanExpression (context, ctx);
    }

    @Override
    public LogicalOrBinaryBooleanExpression visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        return new LogicalOrBinaryBooleanExpression (context, ctx);
    }

    @Override
    public Node visitBinaryReferenceEqualityExpression (@NotNull BrygParser.BinaryReferenceEqualityExpressionContext ctx) {
        return new ReferenceEqualityExpression (context, ctx);
    }


    //
    //  Unary
    //

    @Override
    public @Nullable Expression visitVariable (@NotNull BrygParser.VariableContext ctx) {
        String id = IdUtil.idToString (ctx.id ());
        if (id != null) {
            Variable variable = context.getCurrentScope ().getVariable (id);

            if (variable != null) {
                return new VariableExpression (context, variable, AccessMode.get, ctx.getStart ().getLine ());
            }else { /* The variable is probably a function. */
                Function function = context.getLibrary ().getFunction (id);
                if (function != null) {
                    return new FunctionCallExpression (context, function, ctx.getStart ().getLine ());
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
            return new AccessExpression (context, ctx, AccessMode.get);
        } catch (NoSuchFieldException e) {
            e.printStackTrace ();
            return null;
        }
    }

    @Override
    public CastExpression visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        return new CastExpression (context, ctx);
    }

    @Override
    public IncDecExpression visitUnaryPostfixExpression (@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        return new IncDecExpression (context, ctx.expression (), op == BrygLexer.INC, false, line);
    }

    @Override
    public Node visitUnaryPrefixExpression (@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        if (op == BrygLexer.MINUS) {
            return new NegationExpression (context, (Expression) visit (ctx.expression ()), line);
        }else if (op == BrygLexer.PLUS) {
            return super.visitUnaryPrefixExpression (ctx); /* A unary plus does nothing, so let the
                                                              visitor check the child. */
        }else {
            return new IncDecExpression (context, ctx.expression (), op == BrygLexer.INC, true, line);
        }
    }

    @Override
    public Expression visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        final int op = ctx.op.getType ();
        if (op == BrygLexer.BNOT) {
            return new BitwiseNotExpression (context, ctx.expression ());
        }else { /* NOT */
            return new LogicalNotBooleanExpression (context, ctx.expression ());
        }
    }


    //
    //  Bitwise
    //

    @Override
    public BinaryBitwiseAndExpression visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        return new BinaryBitwiseAndExpression (context, ctx);
    }

    @Override
    public BinaryBitwiseXorExpression visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        return new BinaryBitwiseXorExpression (context, ctx);
    }

    @Override
    public BinaryBitwiseOrExpression visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        return new BinaryBitwiseOrExpression (context, ctx);
    }


    //
    //  Shift
    //

    @Override
    public BinaryShiftExpression visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        int op = ctx.op.getType ();
        if (op == BrygLexer.SIG_LSHIFT) {
            return new BinarySignedLeftShiftExpression (context, ctx);
        }else if (op == BrygLexer.SIG_RSHIFT) {
            return new BinarySignedRightShiftExpression (context, ctx);
        }else { /* UNSIG_RSHIFT */
            return new BinaryUnsignedRightShiftExpression (context, ctx);
        }
    }


    //
    //  Assignment
    //

    @Override
    public Node visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression (context, ctx);
    }


    //
    //  Literal
    //

    @Override
    public IntegerLiteralExpression visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new IntegerLiteralExpression (context, ctx);
    }

    @Override
    public DoubleLiteralExpression visitDoubleLiteral (@NotNull BrygParser.DoubleLiteralContext ctx) {
        return new DoubleLiteralExpression (context, ctx);
    }

    @Override
    public FloatLiteralExpression visitFloatLiteral (@NotNull BrygParser.FloatLiteralContext ctx) {
        return new FloatLiteralExpression (context, ctx);
    }

    @Override
    public Expression visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        String text = ctx.getText ();
        int line = ctx.getStart ().getLine ();
        return InterpolationUtil.compileString (context, text, line);
    }

    @Override
    public ObjectLiteralExpression visitNullLiteral (@NotNull BrygParser.NullLiteralContext ctx) {
        return new ObjectLiteralExpression (context, null, ctx.getStart ().getLine ());
    }

    @Override
    public Node visitBooleanLiteral (@NotNull BrygParser.BooleanLiteralContext ctx) {
        return new BooleanLiteralExpression (context, ctx);
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
