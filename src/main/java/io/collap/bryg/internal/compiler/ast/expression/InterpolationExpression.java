package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.StringBuilderCompileHelper;
import io.collap.bryg.internal.type.Types;

import java.util.List;

// TODO: If the interpolation is directly written to the Writer, we can add the possible constant beginning and end of
//       the interpolation to the constant string writer.

public class InterpolationExpression extends Expression {

    List<Expression> expressions;

    public InterpolationExpression(CompilationContext compilationContext, List<Expression> expressions, int line) {
        super(compilationContext, line);
        setType(Types.fromClass(String.class));
        this.expressions = expressions;
    }

    @Override
    public void compile() {
        StringBuilderCompileHelper builder = new StringBuilderCompileHelper(compilationContext.getMethodVisitor());

        builder.compileNew();
        // -> StringBuilder

        for (Expression expression : expressions) {
            // Don't write constant expressions that are empty.
            if (expression.isConstant() && ((String) expression.getConstantValue()).isEmpty()) {
                continue;
            }

            builder.compileAppend(expression);
            // StringBuilder -> StringBuilder
        }

        builder.compileToString();
        // StringBuilder -> String
    }

}
