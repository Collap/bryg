package io.collap.bryg.compiler.ast.expression.unary;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.NOP;

public class CastExpression extends Expression {

    private Expression child;
    private int conversionOpcode;

    public CastExpression (Context context, BrygParser.CastExpressionContext ctx) {
        this (
                context,
                new TypeInterpreter (context.getEnvironment ().getClassResolver ()).interpretType (ctx.type ()),
                (Expression) context.getParseTreeVisitor ().visit (ctx.expression ()),
                ctx.getStart ().getLine ()
        );
    }

    public CastExpression (Context context, Type targetType, Expression child, int line) {
        super (context);
        this.child = child;
        setType (targetType);
        setLine (line);

        Type from = child.getType ();
        Type to = type;
        if (from.getJavaType ().isPrimitive () && to.getJavaType ().isPrimitive ()) {
            conversionOpcode = CoercionUtil.getConversionOpcode (from, to, getLine ());
        }else {
            throw new BrygJitException ("Only casts between primitive types are currently supported", getLine ());
        }
    }

    public CastExpression (Context context, Type targetType, Expression child, int conversionOpcode, int line) {
        super (context);
        this.child = child;
        this.conversionOpcode = conversionOpcode;
        setType (targetType);
        setLine (line);
    }

    @Override
    public void compile () {
        child.compile ();
        // -> from

        if (conversionOpcode != NOP) {
            context.getMethodVisitor ().visitInsn (conversionOpcode);
            // from -> to
        }
    }

}
