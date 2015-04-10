package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.ast.BlockNode;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.module.Function;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class FunctionCallExpression extends Expression {

    private Function function;
    private List<ArgumentExpression> argumentExpressions;
    private Node statementOrBlock;

    public FunctionCallExpression (CompilationContext compilationContext, BrygParser.BlockFunctionCallContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = compilationContext.getParseTreeVisitor ().visitBlock (ctx.block ());
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (CompilationContext compilationContext, BrygParser.FunctionCallContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = new BlockNode (compilationContext);
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (CompilationContext compilationContext, BrygParser.StatementFunctionCallContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = compilationContext.getParseTreeVisitor ().visitStatement (ctx.statement ());
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (CompilationContext compilationContext, Function function, int line) {
        super (compilationContext);
        setLine (line);

        this.function = function;
        statementOrBlock = new BlockNode (compilationContext);
        initArguments (null);

        setType (function.getReturnType ());
    }

    private void initFunction (String name) {
        function = compilationContext.getEnvironment ().getLibrary ().getFunction (name);
        if (function == null) {
            throw new BrygJitException ("Function '" + name + "' not found!", getLine ());
        }

        setType (function.getReturnType ());
    }

    /**
     * Creates a List of ArgumentExpressions and assigns it to 'argumentExpressions'.
     *
     * @param argumentListCtx null: Creates an empty list.
     */
    private void initArguments (@Nullable BrygParser.ArgumentListContext argumentListCtx) {
        if (argumentListCtx != null) {
            argumentExpressions = FunctionUtil.parseArgumentList (compilationContext, argumentListCtx);
        }else {
            argumentExpressions = new ArrayList<> ();
        }
    }

    @Override
    public void compile () {
        function.compile (compilationContext, this);
    }

    public List<ArgumentExpression> getArgumentExpressions () {
        return argumentExpressions;
    }

    public Node getStatementOrBlock () {
        return statementOrBlock;
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        if (statementOrBlock != null) {
            statementOrBlock.print (out, depth + 1);
        }
    }

}
