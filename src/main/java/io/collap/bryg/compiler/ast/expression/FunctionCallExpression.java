package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.BlockNode;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.util.FunctionUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class FunctionCallExpression extends Expression {

    private Function function;
    private List<ArgumentExpression> argumentExpressions;
    private Node statementOrBlock;

    public FunctionCallExpression (Context context, BrygParser.BlockFunctionCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = context.getParseTreeVisitor ().visitBlock (ctx.block ());
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (Context context, BrygParser.FunctionCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = new BlockNode (context);
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (Context context, BrygParser.StatementFunctionCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        initFunction (IdUtil.idToString (ctx.id ()));
        statementOrBlock = context.getParseTreeVisitor ().visitStatement (ctx.statement ());
        initArguments (ctx.argumentList ());
    }

    public FunctionCallExpression (Context context, Function function, int line) {
        super (context);
        setLine (line);

        this.function = function;
        statementOrBlock = new BlockNode (context);
        initArguments (null);

        setType (function.getReturnType ());
    }

    private void initFunction (String name) {
        function = context.getEnvironment ().getLibrary ().getFunction (name);
        if (function == null) {
            throw new BrygJitException ("Function " + name + " not found!", getLine ());
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
            argumentExpressions = FunctionUtil.parseArgumentList (context, argumentListCtx);
        }else {
            argumentExpressions = new ArrayList<> ();
        }
    }

    @Override
    public void compile () {
        function.compile (context, this);
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
