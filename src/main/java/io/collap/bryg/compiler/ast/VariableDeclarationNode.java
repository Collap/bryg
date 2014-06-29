package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static org.objectweb.asm.Opcodes.F_APPEND;
import static org.objectweb.asm.Opcodes.ISTORE;

public class VariableDeclarationNode extends Node {

    private Variable variable;
    private Expression expression;

    // TODO: Add semantics for mut and val.

    public VariableDeclarationNode (StandardVisitor visitor, BrygParser.VariableDeclarationContext ctx) {
        super (visitor);
        setLine (ctx.getStart ().getLine ());

        String name = ctx.Id ().getText ();
        Type expectedType = null;
        if (ctx.type () != null) {
            expectedType = new TypeInterpreter (visitor).interpretType (ctx.type ());
        }

        expression = null;
        if (ctx.expression () != null) {
            expression = (Expression) visitor.visit (ctx.expression ());
        }

        Type type = null;
        if (expectedType == null) {
            if (expression == null) {
                throw new BrygJitException ("Could not infer type for variable " + name + "!", getLine ());
            }else{
                type = expression.getType ();
            }
        }else {
            if (expression == null) {
                type = expectedType;
            }else {
                if (!expression.getType ().equals (expectedType)) {
                    throw new BrygJitException ("The expected type and inferred type do not match for the variable " + name + "!",
                        getLine ());
                }
            }
        }

        variable = visitor.getCurrentScope ().registerVariable (name, type);
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        if (expression != null) {
            expression.compile ();
            // -> value

            org.objectweb.asm.Type asmType = variable.getType ().getAsmType ();
            method.visitVarInsn (asmType.getOpcode (ISTORE), variable.getId ());
            method.visitFrame (F_APPEND, 1, new Object[] { asmType.getInternalName () }, 0, null);
            // T ->
        }else {
            throw new UnsupportedOperationException ("Currently a variable must be declared with an expression, " +
                "default values for types are not yet implemented!");
        }

    }

}
