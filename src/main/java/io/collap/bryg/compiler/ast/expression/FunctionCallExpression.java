package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.BlockNode;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.parser.BrygParser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class FunctionCallExpression extends Expression {

    private Function function;
    private List<ArgumentExpression> argumentExpressions = new ArrayList<> ();
    private Node statementOrBlock;

    public FunctionCallExpression (StandardVisitor visitor, BrygParser.FunctionCallContext ctx) {
        super (visitor);

        String name = ctx.Id ().getText ();
        function = visitor.getLibrary ().getFunction (name);
        if (function == null) {
            throw new RuntimeException ("Function " + name + " not found!");
        }

        setType (function.getReturnType ());

        BrygParser.StatementOrBlockContext blockCtx = ctx.statementOrBlock ();
        if (blockCtx != null) {
            statementOrBlock = visitor.visitStatementOrBlock (blockCtx);
        }else {
            statementOrBlock = new BlockNode (visitor);
        }

        /* Arguments. */
        List<BrygParser.ArgumentContext> argumentContexts = ctx.argument ();
        for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
            argumentExpressions.add (new ArgumentExpression (visitor, argumentContext));
        }
    }

    public FunctionCallExpression (StandardVisitor visitor, Function function) {
        super (visitor);
        this.function = function;
        statementOrBlock = new BlockNode (visitor);

        setType (function.getReturnType ());
    }

    @Override
    public void compile () {
        function.compile (visitor, this);
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
