package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.ISTORE;

public class VariableDeclarationNode extends Node {

    private Variable variable;
    private Expression expression;
    private boolean coerceExpression;
    private Type boxedType;

    public VariableDeclarationNode (Context context, BrygParser.VariableDeclarationContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        String name = IdUtil.idToString (ctx.id ());
        Type expectedType = null;
        if (ctx.type () != null) {
            expectedType = new TypeInterpreter (context.getClassResolver ()).interpretType (ctx.type ());
        }

        expression = null;
        if (ctx.expression () != null) {
            expression = (Expression) context.getParseTreeVisitor ().visit (ctx.expression ());
        }

        Type type = null;
        if (expectedType == null) {
            if (expression == null) {
                throw new BrygJitException ("Could not infer type for variable '" + name + "'.", getLine ());
            }else{
                type = expression.getType ();
            }
        }else {
            if (expression == null) {
                type = expectedType;
            }else {
                if (!expression.getType ().similarTo (expectedType)) {
                    coerceExpression = CoercionUtil.isUnaryCoercionPossible (context, expression, expectedType);
                    if (!coerceExpression) {
                        /* Try to box the value. This is only possible in a few instances and hence not appropriate for coercion. */
                        boxedType = BoxingUtil.boxType (expression.getType ());
                        if (!boxedType.similarTo (expectedType)) {
                            throw new BrygJitException ("The expected type and inferred type do not match for variable '"
                                    + name + "'. Coercion and boxing is futile.", getLine ());
                        }
                    }
                }
                type = expectedType;
            }
        }

        if (type == null) {
            throw new BrygJitException ("Could not get type for variable '" + name + "'.", getLine ());
        }

        variable = context.getCurrentScope ().registerVariable (name, type, ctx.mutability.getType () == BrygLexer.MUT);
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (expression != null) {
            if (coerceExpression) {
                CoercionUtil.attemptUnaryCoercion (context, expression, variable.getType ());
            }else if (boxedType != null) {
                BoxingUtil.compileBoxing (mv, expression, boxedType);
            }else {
                expression.compile ();
            }
            // -> T

            bryg.org.objectweb.asm.Type asmType = variable.getType ().getAsmType ();
            mv.visitVarInsn (asmType.getOpcode (ISTORE), variable.getId ());
            // T ->
        }else {
            throw new UnsupportedOperationException ("Currently a variable must be declared with an expression, " +
                "default values for types are not yet implemented!");
        }
    }

}
