package io.collap.bryg.internal.compiler.ast.expression.unary;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.*;

public class CastExpression extends Expression {

    private Expression child;
    private boolean isPrimitiveCast;
    private int conversionOpcode;

    public CastExpression(CompilationContext compilationContext, BrygParser.CastExpressionContext ctx) {
        this(
                compilationContext,
                new TypeInterpreter(compilationContext.getEnvironment().getClassResolver()).interpretType(ctx.type()),
                (Expression) compilationContext.getParseTreeVisitor().visit(ctx.expression()),
                ctx.getStart().getLine()
        );
    }

    public CastExpression(CompilationContext compilationContext, Type targetType, Expression child, int line) {
        super(compilationContext, line);
        this.child = child;
        setType(targetType);

        Type from = child.getType();
        Type to = getType();
        if (from.isPrimitive() && to.isPrimitive()) {
            conversionOpcode = CoercionUtil.getConversionOpcode(from, to, getLine());
            isPrimitiveCast = true;
        } else {
            isPrimitiveCast = false;
        }
    }

    public CastExpression(CompilationContext compilationContext, Type targetType, Expression child, int conversionOpcode, int line) {
        super(compilationContext, line);
        this.child = child;
        this.conversionOpcode = conversionOpcode;
        isPrimitiveCast = true;
        setType(targetType);
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        child.compile();
        // -> from

        if (isPrimitiveCast) {
            if (conversionOpcode != NOP) {
                mv.visitInsn(conversionOpcode);
                // from -> to
            }
        } else { /* Object cast. */
            mv.visitTypeInsn(CHECKCAST, getType().getInternalName());
            // from -> to
        }
    }

}
