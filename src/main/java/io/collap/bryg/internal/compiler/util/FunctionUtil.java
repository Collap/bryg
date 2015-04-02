package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

public class FunctionUtil {

    public static List<ArgumentExpression> parseArgumentList (Context context, BrygParser.ArgumentListContext ctx) {
        List<ArgumentExpression> expressions = new ArrayList<> ();
        List<BrygParser.ArgumentContext> argumentContexts = ctx.argument ();
        for (BrygParser.ArgumentContext argumentContext : argumentContexts) {
            expressions.add (new ArgumentExpression (context, argumentContext));
        }
        return expressions;
    }

}
