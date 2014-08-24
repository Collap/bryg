package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.ast.*;
import io.collap.bryg.compiler.ast.control.EachStatement;
import io.collap.bryg.compiler.ast.control.IfStatement;
import io.collap.bryg.compiler.ast.control.WhileStatement;
import io.collap.bryg.compiler.ast.expression.*;
import io.collap.bryg.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BitwiseNotExpression;
import io.collap.bryg.compiler.ast.expression.bool.*;
import io.collap.bryg.compiler.ast.expression.literal.DoubleLiteralExpression;
import io.collap.bryg.compiler.ast.expression.literal.FloatLiteralExpression;
import io.collap.bryg.compiler.ast.expression.literal.IntegerLiteralExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.ast.expression.unary.IncDecExpression;
import io.collap.bryg.compiler.ast.expression.unary.NegationExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.expression.Variable;
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

    @Override
    public RootNode visitStart (@NotNull BrygParser.StartContext ctx) {
        return new RootNode (context, ctx);
    }

    @Override
    @Nullable
    public InDeclarationNode visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
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

    @Override
    public Expression visitInterpolation (@NotNull BrygParser.InterpolationContext ctx) {
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
    public @Nullable Expression visitExpressionPrecedenceOrder (@NotNull BrygParser.ExpressionPrecedenceOrderContext ctx) {
        return (Expression) visit (ctx.expression ());
    }

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
    public VariableDeclarationNode visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode (context, ctx);
    }

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
    public Node visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression (context, ctx);
    }


    //
    //  Control Flow
    //

    @Override
    public IfStatement visitIfStatement (@NotNull BrygParser.IfStatementContext ctx) {
        return new IfStatement (context, ctx);
    }

    @Override
    public Node visitEachStatement (@NotNull BrygParser.EachStatementContext ctx) {
        return new EachStatement (context, ctx);
    }

    @Override
    public Node visitWhileStatement (@NotNull BrygParser.WhileStatementContext ctx) {
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
    public Node visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        return new LogicalAndBinaryBooleanExpression (context, ctx);
    }

    @Override
    public Node visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        return new LogicalOrBinaryBooleanExpression (context, ctx);
    }


    //
    //  Unary
    //

    @Override
    public Node visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        return new CastExpression (context, ctx);
    }

    @Override
    public Node visitUnaryPostfixExpression (@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
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
    public Node visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        final int op = ctx.op.getType ();
        if (op == BrygLexer.BNOT) {
            return new BitwiseNotExpression (context, ctx.expression ());
        }else { /* NOT */
            return new LogicalNotBooleanExpression (context, ctx.expression ());
        }
    }


    //
    //  Binary Bitwise
    //

    @Override
    public Node visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        return new BinaryBitwiseAndExpression (context, ctx);
    }

    @Override
    public Node visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        return new BinaryBitwiseXorExpression (context, ctx);
    }

    @Override
    public Node visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        return new BinaryBitwiseOrExpression (context, ctx);
    }


    //
    //  Shift
    //

    @Override
    public Node visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
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
    //  Literals
    //

    @Override
    public Expression visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new IntegerLiteralExpression (context, ctx);
    }

    @Override
    public Node visitDoubleLiteral (@NotNull BrygParser.DoubleLiteralContext ctx) {
        return new DoubleLiteralExpression (context, ctx);
    }

    @Override
    public Node visitFloatLiteral (@NotNull BrygParser.FloatLiteralContext ctx) {
        return new FloatLiteralExpression (context, ctx);
    }

    @Override
    public Expression visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        String text = ctx.getText ();
        int line = ctx.getStart ().getLine ();
        return InterpolationUtil.compileString (context, text, line);
    }

}
