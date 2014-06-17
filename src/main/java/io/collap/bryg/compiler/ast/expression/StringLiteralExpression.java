package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.parser.BrygParser;

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (RenderVisitor visitor, BrygParser.StringLiteralContext ctx) {
        super (visitor);
        setType (ClassType.STRING);
        value = ctx.String ().getText ();
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> String
    }

}
