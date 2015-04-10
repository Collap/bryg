package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

public class FunctionUtil {

    public static List<ArgumentExpression> parseArgumentList (CompilationContext compilationContext, BrygParser.ArgumentListContext ctx) {
        List<ArgumentExpression> expressions = new ArrayList<> ();
        List<BrygParser.ArgumentContext> argumentContexts = ctx.argument ();
        for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
            expressions.add (new ArgumentExpression (compilationContext, argumentContext));
        }
        return expressions;
    }

}
