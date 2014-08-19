package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

public class ArgumentExpression extends Expression {

    private String name;
    private Expression expression;

    public ArgumentExpression (StandardVisitor visitor, BrygParser.ArgumentContext ctx) {
        super (visitor);
        setLine (ctx.getStart ().getLine ());

        BrygParser.ArgumentIdContext id = ctx.argumentId ();
        if (id != null) {
            name = id.getText ();
        }

        expression = (Expression) visitor.visit (ctx.expression ());
        setType (expression.getType ());
    }

    @Override
    public void compile () {
        expression.compile ();
    }

    public String getName () {
        return name;
    }

    @Override
    public Object getConstantValue () {
        return expression.getConstantValue ();
    }

    public Expression getExpression () {
        return expression;
    }

}
