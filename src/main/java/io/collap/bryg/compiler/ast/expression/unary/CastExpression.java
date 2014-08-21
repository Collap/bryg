package io.collap.bryg.compiler.ast.expression.unary;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class CastExpression extends Expression {

    private Expression child;
    private Type targetType;

    public CastExpression (StandardVisitor visitor, BrygParser.CastExpressionContext ctx) {
        this (visitor, new TypeInterpreter (visitor).interpretType (ctx.type ()),
                (Expression) visitor.visit (ctx.expression ()), ctx.getStart ().getLine ());
    }

    public CastExpression (StandardVisitor visitor, Type targetType, Expression child, int line) {
        super (visitor);
        this.targetType = targetType;
        this.child = child;
        setType (targetType);
        setLine (line);
    }

    @Override
    public void compile () {
        child.compile ();
        // -> from

        Type from = child.getType ();
        Type to = targetType;
        if (to.getJavaType ().isPrimitive () && from.getJavaType ().isPrimitive ()) {
            int opcode = CoercionHelper.getConversionOpcode (from, to, getLine ());
            visitor.getMethod ().visitInsn (opcode);
            // from -> to
        }else {
            throw new BrygJitException ("Only casts between primitive types are currently supported", getLine ());
        }
    }

}
