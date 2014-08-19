package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class DoubleLiteralExpression extends Expression {

    private double value;

    public DoubleLiteralExpression (StandardVisitor visitor, BrygParser.DoubleLiteralContext ctx) {
        super (visitor);
        setType (new Type (Double.TYPE));
        setLine (ctx.getStart ().getLine ());

        value = Double.parseDouble (ctx.Double ().getText ());
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> double
    }

}
