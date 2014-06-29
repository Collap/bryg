package io.collap.bryg.compiler.parser;

import io.collap.bryg.compiler.ast.expression.*;
import io.collap.bryg.compiler.ast.expression.bool.EqualityBinaryBooleanExpression;
import io.collap.bryg.compiler.ast.expression.bool.LogicalAndBinaryBooleanExpression;
import io.collap.bryg.compiler.ast.expression.bool.LogicalOrBinaryBooleanExpression;
import io.collap.bryg.compiler.ast.expression.bool.RelationalBinaryBooleanExpression;
import io.collap.bryg.compiler.expression.*;
import io.collap.bryg.compiler.ast.*;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygBaseVisitor;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.model.Model;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.Map;

public class StandardVisitor extends BrygBaseVisitor<Node> {

    private BrygMethodVisitor method;
    private Scope rootScope = new RootScope ();
    private Scope currentScope = rootScope; /* The current scope is the scope each node resides in at its creation. */
    private Library library;
    private ClassResolver classResolver;
    private Map<Integer, Integer> lineToSourceLineMap;

    public StandardVisitor (BrygMethodVisitor method, Library library, ClassResolver classResolver, Map lineToSourceLineMap) {
        this.method = method;
        this.library = library;
        this.classResolver = classResolver;
        this.lineToSourceLineMap = lineToSourceLineMap;

        /* Register parameters in the correct order. */
        rootScope.registerVariable ("this", null); // TODO: Proper type.
        rootScope.registerVariable ("writer", new Type (Writer.class));
        rootScope.registerVariable ("model", new Type (Model.class));
    }

    @Override
    public Node visitStart (@NotNull BrygParser.StartContext ctx) {
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
    public Node visitBlock (@NotNull BrygParser.BlockContext ctx) {
        return new BlockNode (this, ctx);
    }

    @Override
    public AccessExpression visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        try {
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
    @Nullable
    public Expression visitVariable (@NotNull BrygParser.VariableContext ctx) {
        TerminalNode id = ctx.Id ();
        if (id != null) {
            Variable variable = currentScope.getVariable (id.getText ());

            if (variable != null) {
                return new VariableExpression (this, variable, AccessMode.get, ctx.getStart ().getLine ());
            }else { /* The variable is probably a function. */
                Function function = library.getFunction (id.getText ());
                if (function != null) {
                    return new FunctionCallExpression (this, function, ctx.getStart ().getLine ());
                }
            }

            throw new RuntimeException ("Variable " + id.getText () + " not found!");
        }

        return null;
    }

    @Override
    public Node visitVariableDeclaration (@NotNull BrygParser.VariableDeclarationContext ctx) {
        return new VariableDeclarationNode (this, ctx);
    }

    @Override
    public Expression visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        return new FunctionCallExpression (this, ctx);
    }

    @Override
    public Node visitBinaryAssignmentExpression (@NotNull BrygParser.BinaryAssignmentExpressionContext ctx) {
        return new BinaryAssignmentExpression (this, ctx);
    }

    @Override
    public BinaryAdditionExpression visitBinaryAdditionExpression (@NotNull BrygParser.BinaryAdditionExpressionContext ctx) {
        return new BinaryAdditionExpression (this, ctx);
    }

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

    @Override
    public Expression visitIntegerLiteral (@NotNull BrygParser.IntegerLiteralContext ctx) {
        return new IntegerLiteralExpression (this, ctx);
    }

    @Override
    public Expression visitStringLiteral (@NotNull BrygParser.StringLiteralContext ctx) {
        return new StringLiteralExpression (this, ctx);
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

    public Map<Integer, Integer> getLineToSourceLineMap () {
        return lineToSourceLineMap;
    }

}
