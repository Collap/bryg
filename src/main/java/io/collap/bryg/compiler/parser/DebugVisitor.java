package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.omg.CORBA.IDLTypeHelper;

public class DebugVisitor extends BrygParserBaseVisitor<Integer> {

    @Override
    public Integer visitBinaryIsExpression (@NotNull BrygParser.BinaryIsExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryIsExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitUnarySuffixExpression (@NotNull BrygParser.UnarySuffixExpressionContext ctx) {
        visitAny (ctx);
        super.visitUnarySuffixExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        visitAny (ctx);
        super.visitAccessExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryBitwiseOrExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryAdditionExpression (@NotNull BrygParser.BinaryAdditionExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryAdditionExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryShiftExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitExpressionPrecedenceOrder (@NotNull BrygParser.ExpressionPrecedenceOrderContext ctx) {
        visitAny (ctx);
        super.visitExpressionPrecedenceOrder (ctx);
        return 0;
    }

    @Override
    public Integer visitUnaryPrefixExpression (@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        visitAny (ctx);
        super.visitUnaryPrefixExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryLogicalAndExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryRelationalExpression (@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryRelationalExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitVariableExpression (@NotNull BrygParser.VariableExpressionContext ctx) {
        visitAny (ctx);
        super.visitVariableExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryEqualityExpression (@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryEqualityExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryMultiplicationExpression (@NotNull BrygParser.BinaryMultiplicationExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryMultiplicationExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitLiteralExpression (@NotNull BrygParser.LiteralExpressionContext ctx) {
        visitAny (ctx);
        super.visitLiteralExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryAssignmentExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryBitwiseAndExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryLogicalOrExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        visitAny (ctx);
        super.visitUnaryOperationExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        visitAny (ctx);
        super.visitCastExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitFunctionCallExpression (@NotNull BrygParser.FunctionCallExpressionContext ctx) {
        visitAny (ctx);
        super.visitFunctionCallExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        visitAny (ctx);
        super.visitBinaryBitwiseXorExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitArgument (@NotNull BrygParser.ArgumentContext ctx) {
        String name = ctx.argumentId ().getText ();
        visitAny (ctx, name);
        super.visitArgument (ctx);
        return 0;
    }

    @Override
    public Integer visitIfExpression (@NotNull BrygParser.IfExpressionContext ctx) {
        visitAny (ctx);
        super.visitIfExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitBlock (@NotNull BrygParser.BlockContext ctx) {
        visitAny (ctx);
        super.visitBlock (ctx);
        return 0;
    }

    @Override
    public Integer visitType (@NotNull BrygParser.TypeContext ctx) {
        visitAny (ctx);
        super.visitType (ctx);
        return 0;
    }

    @Override
    public Integer visitEachExpression (@NotNull BrygParser.EachExpressionContext ctx) {
        visitAny (ctx);
        super.visitEachExpression (ctx);
        return 0;
    }

    @Override
    public Integer visitStatementLine (@NotNull BrygParser.StatementLineContext ctx) {
        visitAny (ctx);
        super.visitStatementLine (ctx);
        return 0;
    }

    @Override
    public Integer visitStatement (@NotNull BrygParser.StatementContext ctx) {
        visitAny (ctx);
        super.visitStatement (ctx);
        return 0;
    }

    @Override
    public Integer visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        visitAny (ctx);
        super.visitVariableDeclaration (ctx);
        return 0;
    }

    @Override
    public Integer visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
        visitAny (ctx);
        super.visitInDeclaration (ctx);
        return 0;
    }

    @Override
    public Integer visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        visitAny (ctx, IdHelper.idToString (ctx.id ()));
        super.visitFunctionCall (ctx);
        return 0;
    }

    @Override
    public Integer visitStart (@NotNull BrygParser.StartContext ctx) {
        visitAny (ctx);
        super.visitStart (ctx);
        return 0;
    }

    @Override
    public Integer visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        visitAny (ctx, ctx.String ().getSymbol ().getText ());
        super.visitStringLiteral (ctx);
        return 0;
    }

    @Override
    public Integer visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        visitAny (ctx, ctx.Integer ().getSymbol ().getText ());
        super.visitIntegerLiteral (ctx);
        return 0;
    }

    @Override
    public Integer visitVariable (@NotNull BrygParser.VariableContext ctx) {
        visitAny (ctx);
        super.visitVariable (ctx);
        return 0;
    }

    private Integer visitAny (ParserRuleContext ctx) {
        return visitAny (ctx, null);
    }

    private Integer visitAny (ParserRuleContext ctx, String info) {
        String line = "";
        int depth = ctx.depth ();
        for (int i = 0; i < depth; ++i) {
            line += "  ";
        }
        line += BrygParser.ruleNames[ctx.getRuleIndex ()];
        line += "[" + ctx.getClass ().getSimpleName () + "]";
        if (info != null) {
            line += " (" + info + ")";
        }
        System.out.println (line);
        return 0;
    }

}
