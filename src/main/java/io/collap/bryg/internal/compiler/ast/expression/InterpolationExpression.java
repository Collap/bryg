package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.util.StringBuilderCompileHelper;
import io.collap.bryg.internal.type.Types;

import java.util.List;

public class InterpolationExpression extends Expression {

    List<Expression> expressions;

    public InterpolationExpression (Context context, List<Expression> expressions, int line) {
        super (context);
        setType (Types.fromClass (String.class));
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
