package io.collap.bryg.compiler.ast.expression.unary;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public class CastExpression extends Expression {

    private Expression child;
    private Type targetType;

    public CastExpression (Context context, BrygParser.CastExpressionContext ctx) {
        this (
                context,
                new TypeInterpreter (context.getClassResolver ()).interpretType (ctx.type ()),
                (Expression) context.getParseTreeVisitor ().visit (ctx.expression ()),
                ctx.getStart ().getLine ()
        );
    }

    public CastExpression (Context context, Type targetType, Expression child, int line) {
        super (context);
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
            int opcode = CoercionUtil.getConversionOpcode (from, to, getLine ());
            context.getMethodVisitor ().visitInsn (opcode);
            // from -> to
        }else {
            throw new BrygJitException ("Only casts between primitive types are currently supported", getLine ());
        }
    }

}
