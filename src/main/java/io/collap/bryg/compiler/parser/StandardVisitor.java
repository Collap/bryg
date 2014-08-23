package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.ast.expression.*;
import io.collap.bryg.compiler.ast.expression.arithmetic.*;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseAndExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseOrExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BinaryBitwiseXorExpression;
import io.collap.bryg.compiler.ast.expression.bool.*;
import io.collap.bryg.compiler.ast.expression.literal.DoubleLiteralExpression;
import io.collap.bryg.compiler.ast.expression.literal.FloatLiteralExpression;
import io.collap.bryg.compiler.ast.expression.literal.IntegerLiteralExpression;
import io.collap.bryg.compiler.ast.expression.bitwise.BitwiseNotExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedLeftShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinarySignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.shift.BinaryUnsignedRightShiftExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.ast.expression.unary.IncDecExpression;
import io.collap.bryg.compiler.ast.expression.unary.NegationExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.expression.*;
import io.collap.bryg.compiler.ast.*;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.helper.InterpolationHelper;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParserBaseVisitor;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.model.Model;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;
import java.io.Writer;

public class StandardVisitor extends BrygParserBaseVisitor<Node> {

    private BrygMethodVisitor method;
    private Scope rootScope = new RootScope ();
    private Scope currentScope = rootScope; /* The current scope is the scope each node resides in at its creation. */
    private Library library;
    private ClassResolver classResolver;

    public StandardVisitor (BrygMethodVisitor method, Library library, ClassResolver classResolver) {
        this.method = method;
        this.library = library;
        this.classResolver = classResolver;

        /* Register parameters in the correct order. */
        rootScope.registerVariable ("this", null); // TODO: Proper type.
        rootScope.registerVariable ("writer", new Type (Writer.class));
        rootScope.registerVariable ("model", new Type (Model.class));
    }

    @Override
    public RootNode visitStart (@NotNull BrygParser.StartContext ctx) {
        return new RootNode (this, ctx);
    }

    @Override
    @Nullable
    public InDeclarationNode visitInDeclaration (@NotNull BrygParser.InDeclarationContext ctx) {
        try {
            return new InDeclarationNode (this, ctx);
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }
        return null;
    }

    @Override
    public StatementNode visitStatement (@NotNull BrygParser.StatementContext ctx) {
        return new StatementNode (this, ctx);
    }

    @Override
    public BlockNode visitBlock (@NotNull BrygParser.BlockContext ctx) {
        return new BlockNode (this, ctx);
    }

    @Override
    public Expression visitInterpolation (@NotNull BrygParser.InterpolationContext ctx) {
        return (Expression) visit (ctx.expression ());
    }

    @Override
    public AccessExpression visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        try {
            /* Note: This corresponds to the getter only, the setter scenario is handled by the assignment expression! */
            return new AccessExpression (this, ctx, AccessMode.get);
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
        String id = IdHelper.idToString (ctx.id ());
        if (id != null) {
            Variable variable = currentScope.getVariable (id);

            if (variable != null) {
                return new VariableExpression (this, variable, AccessMode.get, ctx.getStart ().getLine ());
            }else { /* The variable is probably a function. */
                Function function = library.getFunction (id);
                if (function != null) {
                    return new FunctionCallExpression (this, function, ctx.getStart ().getLine ());
                }
            }

            throw new BrygJitException ("Variable " + id + " not found.", ctx.getStart ().getLine ());
        }

        return null;
    }

    @Override
    public VariableDeclarationNode visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode (this, ctx);
    }

    @Override
    public FunctionCallExpression visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        return new FunctionCallExpression (this, ctx);
    }

    @Override
    public FunctionCallExpression visitBlockFunctionCall (@NotNull BrygParser.BlockFunctionCallContext ctx) {
        return new FunctionCallExpression (this, ctx);
    }

    @Override
    public FunctionCallExpression visitStatementFunctionCall (@NotNull BrygParser.StatementFunctionCallContext ctx) {
        return new FunctionCallExpression (this, ctx);
    }

    @Override
    public Node visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression (this, ctx);
    }


    //
    //  Arithmetic
    //

    @Override
    public BinaryArithmeticExpression visitBinaryAddSubExpression (@NotNull BrygParser.BinaryAddSubExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        if (ctx.op.getType () == BrygLexer.PLUS) {
            return new BinaryAdditionExpression (this, left, right);
        }else { /* MINUS */
            return new BinarySubtractionExpression (this, left, right);
        }
    }

    @Override
    public Node visitBinaryMulDivRemExpression (@NotNull BrygParser.BinaryMulDivRemExpressionContext ctx) {
        BrygParser.ExpressionContext left = ctx.expression (0);
        BrygParser.ExpressionContext right = ctx.expression (1);

        int op = ctx.op.getType ();
        if (op == BrygLexer.MUL) {
            return new BinaryMultiplicationExpression (this, left, right);
        }else if (op == BrygLexer.DIV) {
            return new BinaryDivisionExpression (this, left, right);
        }else { /* REM */
            return new BinaryRemainderExpression (this, left, right);
        }
    }


    //
    //  Relational
    //

    @Override
    public EqualityBinaryBooleanExpression visitBinaryEqualityExpression (@NotNull BrygParser.BinaryEqualityExpressionContext ctx) {
        return new EqualityBinaryBooleanExpression (this, ctx);
    }

    @Override
    public RelationalBinaryBooleanExpression visitBinaryRelationalExpression (@NotNull BrygParser.BinaryRelationalExpressionContext ctx) {
        return new RelationalBinaryBooleanExpression (this, ctx);
    }

    @Override
    public Node visitBinaryLogicalAndExpression (@NotNull BrygParser.BinaryLogicalAndExpressionContext ctx) {
        return new LogicalAndBinaryBooleanExpression (this, ctx);
    }

    @Override
    public Node visitBinaryLogicalOrExpression (@NotNull BrygParser.BinaryLogicalOrExpressionContext ctx) {
        return new LogicalOrBinaryBooleanExpression (this, ctx);
    }

    @Override
    public IfExpression visitIfExpression (@NotNull BrygParser.IfExpressionContext ctx) {
        return new IfExpression (this, ctx);
    }

    @Override
    public Node visitEachExpression (@NotNull BrygParser.EachExpressionContext ctx) {
        return new EachExpression (this, ctx);
    }


    //
    //  Unary
    //

    @Override
    public Node visitCastExpression (@NotNull BrygParser.CastExpressionContext ctx) {
        return new CastExpression (this, ctx);
    }

    @Override
    public Node visitUnaryPostfixExpression (@NotNull BrygParser.UnaryPostfixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        return new IncDecExpression (this, ctx.expression (), op == BrygLexer.INC, false, line);
    }

    @Override
    public Node visitUnaryPrefixExpression (@NotNull BrygParser.UnaryPrefixExpressionContext ctx) {
        final int op = ctx.op.getType ();
        final int line = ctx.getStart ().getLine ();
        if (op == BrygLexer.MINUS) {
            return new NegationExpression (this, (Expression) visit (ctx.expression ()), line);
        }else if (op == BrygLexer.PLUS) {
            return super.visitUnaryPrefixExpression (ctx); /* A unary plus does nothing, so let the
                                                              visitor check the child. */
        }else {
            return new IncDecExpression (this, ctx.expression (), op == BrygLexer.INC, true, line);
        }
    }

    @Override
    public Node visitUnaryOperationExpression (@NotNull BrygParser.UnaryOperationExpressionContext ctx) {
        final int op = ctx.op.getType ();
        if (op == BrygLexer.BNOT) {
            return new BitwiseNotExpression (this, ctx.expression ());
        }else { /* NOT */
            // TODO: Implement
            return new LogicalNotBooleanExpression (this, ctx.expression ());
        }
    }


    //
    //  Binary Bitwise
    //

    @Override
    public Node visitBinaryBitwiseAndExpression (@NotNull BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        return new BinaryBitwiseAndExpression (this, ctx);
    }

    @Override
    public Node visitBinaryBitwiseXorExpression (@NotNull BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        return new BinaryBitwiseXorExpression (this, ctx);
    }

    @Override
    public Node visitBinaryBitwiseOrExpression (@NotNull BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        return new BinaryBitwiseOrExpression (this, ctx);
    }


    //
    //  Shift
    //

    @Override
    public Node visitBinaryShiftExpression (@NotNull BrygParser.BinaryShiftExpressionContext ctx) {
        int op = ctx.op.getType ();
        if (op == BrygLexer.SIG_LSHIFT) {
            return new BinarySignedLeftShiftExpression (this, ctx);
        }else if (op == BrygLexer.SIG_RSHIFT) {
            return new BinarySignedRightShiftExpression (this, ctx);
        }else { /* UNSIG_RSHIFT */
            return new BinaryUnsignedRightShiftExpression (this, ctx);
        }
    }


    //
    //  Literals
    //

    @Override
    public Expression visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new IntegerLiteralExpression (this, ctx);
    }

    @Override
    public Node visitDoubleLiteral (@NotNull BrygParser.DoubleLiteralContext ctx) {
        return new DoubleLiteralExpression (this, ctx);
    }

    @Override
    public Node visitFloatLiteral (@NotNull BrygParser.FloatLiteralContext ctx) {
        return new FloatLiteralExpression (this, ctx);
    }

    @Override
    public Expression visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        String text = ctx.getText ();
        int line = ctx.getStart ().getLine ();
        return InterpolationHelper.compileString (this, text, line);
    }




    public BrygMethodVisitor getMethod () {
        return method;
    }

    public Scope getCurrentScope () {
        return currentScope;
    }

    public void setCurrentScope (Scope currentScope) {
        this.currentScope = currentScope;
    }

    public Library getLibrary () {
        return library;
    }

    public ClassResolver getClassResolver () {
        return classResolver;
    }

    public Scope getRootScope () {
        return rootScope;
    }

}
