package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.type.Type;

import java.util.List;

public class InterpolationExpression extends Expression {

    List<Expression> expressions;

    public InterpolationExpression (Context context, List<Expression> expressions, int line) {
        super (context);
        setType (new Type (String.class));
        setLine (line);
        this.expressions = expressions;
    }

    @Override
    public void compile () {
        StringBuilderCompileHelper builder = new StringBuilderCompileHelper (context.getMethodVisitor ());

        builder.compileNew ();
        // -> StringBuilder

        for (Expression expression : expressions) {
            builder.compileAppend (expression);
            // StringBuilder -> StringBuilder
        }

        builder.compileToString ();
        // StringBuilder -> String
    }

}
