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
import io.collap.bryg.parser.BrygBaseVisitor;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.model.Model;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;
import java.io.Writer;

public class RenderVisitor extends BrygBaseVisitor<Node> {

    private BrygMethodVisitor method;
    private Scope scope = new Scope ();
    private Library library;
    private ClassResolver classResolver;

    public RenderVisitor (BrygMethodVisitor method, Library library, ClassResolver classResolver) {
        this.method = method;
        this.library = library;
        this.classResolver = classResolver;

        /* Register parameters in the correct order. */
        scope.registerVariable ("this", null); // TODO: Proper type.
        scope.registerVariable ("writer", new ClassType (Writer.class));
        scope.registerVariable ("model", new ClassType (Model.class));
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
    public TextCommandNode visitTextCommand (@NotNull BrygParser.TextCommandContext ctx) {
        return new TextCommandNode (this, ctx.Text ().getText ());
    }

    @Override
    public AccessExpression visitAccessExpression (@NotNull BrygParser.AccessExpressionContext ctx) {
        try {
            return new AccessExpression (this, ctx);
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
            Variable variable = scope.getVariable (id.getText ());

            System.out.println ("Id: " + id.getText ());

            if (variable != null) {
                return new VariableExpression (this, variable);
            }else { /* The variable is probably a function. */
                Function function = library.getFunction (id.getText ());
                if (function != null) {
                    return new FunctionCallExpression (this, function);
                }
            }

            throw new RuntimeException ("Variable " + id.getText () + " not found!");
        }else { /* Variable declaration. */

        }

        return null;
    }

    @Override
    public Expression visitFunctionCall (@NotNull BrygParser.FunctionCallContext ctx) {
        return new FunctionCallExpression (this, ctx);
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

    public Scope getScope () {
        return scope;
    }

    public Library getLibrary () {
        return library;
    }

    public ClassResolver getClassResolver () {
        return classResolver;
    }

}
