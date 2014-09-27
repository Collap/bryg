package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

// TODO: Can we override 'visit' to invoke printContext? Might reduce a lot of the boilerplate code.

public class PrintTreeVisitor extends BrygParserBaseVisitor<Integer> {

    private void printContext (ParserRuleContext ctx) {
        printContext (ctx, null);
    }

    private void printContext (ParserRuleContext ctx, String info) {
        String line = "";
        int depth = ctx.depth ();
        for (int i = 1; i < depth; ++i) {
            line += "  ";
        }
        line += BrygParser.ruleNames[ctx.getRuleIndex ()];
        line += "[" + ctx.getClass ().getSimpleName () + "]";
        if (info != null) {
            line += " (" + info + ")";
        }
        System.out.println (line);
    }


    //
    //  General
    //

    @Override
    public Integer visitStart (@NotNull BrygParser.StartContext ctx) {
        printContext (ctx);
        super.visitStart (ctx);
        return 0;
    }

    @Override
    public Integer visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
        printContext (ctx);
        super.visitInDeclaration (ctx);
        return 0;
    }

    @Override
    public Integer visitStatement (@NotNull BrygParser.StatementContext ctx) {
        printContext (ctx);
        super.visitStatement (ctx);
        return 0;
    }

    @Override
    public Integer visitBlock (@NotNull BrygParser.BlockContext ctx) {
        printContext (ctx);
        super.visitBlock (ctx);
        return 0;
    }


    //
    //  General Statement
    //

    @Override
    public Integer visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        printContext (ctx);
        super.visitVariableDeclaration (ctx);
        return 0;
    }


    //
    //  Function Call
    //

    @Override
    public Integer visitArgumentList (@NotNull BrygParser.ArgumentListContext ctx) {
        printContext (ctx);
        super.visitArgumentList (ctx);
        return 0;
    }

    @Override
    public Integer visitArgument (@NotNull BrygParser.ArgumentContext ctx) {
        String name;
        BrygParser.ArgumentIdContext id = ctx.argumentId ();
        if (id != null) {
            name = id.getText ();
        }else {
            name = "<unnamed>";
        }
        printContext (ctx, name);
        super.visitArgument (ctx);
        return 0;
    }

    @Override
    public Integer visitFunctionCallExpression (@NotNull BrygParser.FunctionCallExpressionContext ctx) {
        printContext (ctx);
        super.visitFunctionCallExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        printContext (ctx);
        super.visitFunctionCall (ctx);
        return 0;
    }

    @Override
    public Integer visitBlockFunctionCall (@NotNull BrygParser.BlockFunctionCallContext ctx) {
        printContext (ctx);
        super.visitBlockFunctionCall (ctx);
        return 0;
    }

    @Override
    public Integer visitStatementFunctionCall (@NotNull BrygParser.StatementFunctionCallContext ctx) {
        super.visitStatementFunctionCall (ctx);
        return 0;
    }

    @Override
    public Integer visitMethodCallExpression (@NotNull BrygParser.MethodCallExpressionContext ctx) {
        printContext (ctx);
        super.visitMethodCallExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitTemplateFragmentCall (@NotNull BrygParser.TemplateFragmentCallContext ctx) {
        printContext (ctx);
        super.visitTemplateFragmentCall (ctx);
        return 0;
    }


    //
    //  Control Flow
    //

    @Override
    public Integer visitIfStatement (@NotNull BrygParser.IfStatementContext ctx) {
        printContext (ctx);
        super.visitIfStatement (ctx);
        return 0;
    }

    @Override
    public Integer visitEachStatement (@NotNull BrygParser.EachStatementContext ctx) {
        printContext (ctx);
        super.visitEachStatement (ctx);
        return 0;
    }

    @Override
    public Integer visitWhileStatement (@NotNull BrygParser.WhileStatementContext ctx) {
        printContext (ctx);
        super.visitWhileStatement (ctx);
        return 0;
    }


    //
    //  Arithmetic
    //

    @Override
    public Integer visitBinaryAddSubExpression (@NotNull BrygParser.BinaryAddSubExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryAddSubExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryMulDivRemExpression (@NotNull BrygParser.BinaryMulDivRemExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryMulDivRemExpression (ctx);
        return 0;
    }


    //
    //  Relational
    //

    @Override
    public Integer visitBinaryEqualityExpression (@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryEqualityExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryRelationalExpression (@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryRelationalExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryLogicalAndExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryLogicalOrExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryReferenceEqualityExpression (@NotNull BrygParser.BinaryReferenceEqualityExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryReferenceEqualityExpression (ctx);
        return 0;
    }


    //
    //  Unary
    //

    @Override
    public Integer visitVariableExpression (@NotNull BrygParser.VariableExpressionContext ctx) {
        printContext (ctx);
        super.visitVariableExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitVariable (@NotNull BrygParser.VariableContext ctx) {
        printContext (ctx);
        super.visitVariable (ctx);
        return 0;
    }

    @Override
    public Integer visitExpressionPrecedenceOrder (@NotNull BrygParser.ExpressionPrecedenceOrderContext ctx) {
        printContext (ctx);
        super.visitExpressionPrecedenceOrder (ctx);
        return 0;
    }

    @Override
    public Integer visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        printContext (ctx);
        super.visitAccessExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        printContext (ctx);
        super.visitCastExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitUnaryPostfixExpression (@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitUnaryPostfixExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitUnaryPrefixExpression (@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitUnaryPrefixExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitUnaryOperationExpression (ctx);
        return 0;
    }


    //
    //  Bitwise
    //

    @Override
    public Integer visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryBitwiseAndExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryBitwiseOrExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryBitwiseXorExpression (ctx);
        return 0;
    }


    //
    //  Shift
    //

    @Override
    public Integer visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryShiftExpression (ctx);
        return 0;
    }


    //
    //  Assignment
    //

    @Override
    public Integer visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        printContext (ctx, ctx.op.getText ());
        super.visitBinaryAssignmentExpression (ctx);
        return 0;
    }


    //
    //  Special
    //

    @Override
    public Integer visitBinaryIsExpression (@NotNull BrygParser.BinaryIsExpressionContext ctx) {
        printContext (ctx);
        super.visitBinaryIsExpression (ctx);
        return 0;
    }


    //
    //  Literal
    //

    @Override
    public Integer visitLiteralExpression (@NotNull BrygParser.LiteralExpressionContext ctx) {
        printContext (ctx);
        super.visitLiteralExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        printContext (ctx, ctx.Integer ().getText ());
        return 0;
    }

    @Override
    public Integer visitDoubleLiteral (@NotNull BrygParser.DoubleLiteralContext ctx) {
        printContext (ctx, ctx.Double ().getText ());
        return 0;
    }

    @Override
    public Integer visitFloatLiteral (@NotNull BrygParser.FloatLiteralContext ctx) {
        printContext (ctx, ctx.Float ().getText ());
        return 0;
    }

    @Override
    public Integer visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        printContext (ctx, ctx.String ().getText ());
        return 0;
    }

    @Override
    public Integer visitNullLiteral (@NotNull BrygParser.NullLiteralContext ctx) {
        printContext (ctx);
        return 0;
    }

    @Override
    public Integer visitBooleanLiteral (@NotNull BrygParser.BooleanLiteralContext ctx) {
        printContext (ctx, ctx.getText ());
        return 0;
    }


    //
    //  Miscellaneous
    //

    @Override
    public Integer visitType (@NotNull BrygParser.TypeContext ctx) {
        printContext (ctx);
        super.visitType (ctx);
        return 0;
    }


    //
    //  Id
    //

    @Override
    public Integer visitId (@NotNull BrygParser.IdContext ctx) {
        printContext (ctx, IdUtil.idToString (ctx));
        return 0;
    }

    @Override
    public Integer visitArgumentId (@NotNull BrygParser.ArgumentIdContext ctx) {
        printContext (ctx, ctx.getText ());
        return 0;
    }

    @Override
    public Integer visitTemplateId (@NotNull BrygParser.TemplateIdContext ctx) {
        printContext (ctx, ctx.getText ());
        return 0;
    }

}
