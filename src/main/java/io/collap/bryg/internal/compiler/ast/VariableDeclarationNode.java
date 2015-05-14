package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.LocalVariable;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class VariableDeclarationNode extends Node {

    private LocalVariable variable;
    private Expression expression;

    public VariableDeclarationNode(CompilationContext compilationContext, BrygParser.VariableDeclarationContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        String name = IdUtil.idToString(ctx.id());
        @Nullable Type expectedType = null;
        if (ctx.type() != null) {
            expectedType = new TypeInterpreter(compilationContext.getEnvironment()).interpretType(ctx.type());
        }

        @Nullable Expression expression = null;
        if (ctx.expression() != null) {
            expression = (Expression) compilationContext.getParseTreeVisitor().visit(ctx.expression());
        }

        @Nullable Type type;
        if (expectedType == null) {
            if (expression == null) {
                throw new BrygJitException("Could not infer type for variable '" + name + "'.", getLine());
            } else {
                type = expression.getType();
            }
        } else {
            if (expression == null) {
                type = expectedType;
            } else {
                if (!expression.getType().similarTo(expectedType)) {
                    expression = CoercionUtil.applyUnaryCoercion(compilationContext, expression, expectedType);
                }
                type = expression.getType();
            }
        }

        if (type == null) {
            throw new BrygJitException("Could not get type for variable '" + name + "'.", getLine());
        }

        if (expression == null) {
            throw new BrygJitException("Currently a variable must be declared with an expression!", getLine());
        }

        this.expression = expression;
        variable = new LocalVariable(
                type,
                name,
                ctx.mutability.getType() == BrygLexer.MUT ? Mutability.mutable : Mutability.immutable,
                Nullness.notnull
        );
        compilationContext.getCurrentScope().registerLocalVariable(variable);
    }

    @Override
    public void compile() {
        variable.compile(compilationContext, VariableUsageInfo.withSetMode(expression));
    }

}
