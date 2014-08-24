package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

public class VariableExpression extends Expression {

    private Variable variable;
    private AccessMode mode;

    public VariableExpression (Context context, BrygParser.VariableExpressionContext ctx, AccessMode mode) {
        super (context);
        this.mode = mode;
        setLine (ctx.getStart ().getLine ());

        String variableName = IdUtil.idToString (ctx.variable ().id ());
        variable = context.getCurrentScope ().getVariable (variableName);
        if (variable == null) {
            throw new BrygJitException ("Variable " + variableName + " not found!", getLine ());
        }

        setType (variable.getType ());
    }

    public VariableExpression (Context context, Variable variable, AccessMode mode, int line) {
        super (context);
        this.mode = mode;
        setLine (line);

        this.variable = variable;
        setType (variable.getType ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (mode == AccessMode.get) {
            mv.visitVarInsn (type.getAsmType ().getOpcode (ILOAD), variable.getId ());
            // -> T
        }else { /* AccessMode.set */
            mv.visitVarInsn (type.getAsmType ().getOpcode (ISTORE), variable.getId ());
            // T ->
        }
    }

    public Variable getVariable () {
        return variable;
    }

    public AccessMode getMode () {
        return mode;
    }

}
