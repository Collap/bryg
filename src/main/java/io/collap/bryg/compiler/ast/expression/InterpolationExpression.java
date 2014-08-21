package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.helper.StringBuilderCompileHelper;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;

import java.util.List;

public class InterpolationExpression extends Expression {

    List<Expression> expressions;

    public InterpolationExpression (StandardVisitor visitor, List<Expression> expressions, int line) {
        super (visitor);
        setType (new Type (String.class));
        setLine (line);
        this.expressions = expressions;
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();
        StringBuilderCompileHelper builder = new StringBuilderCompileHelper (method);

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
