package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.expression.Variable;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private Variable variable;
    private AccessMode mode;

    public VariableExpression (StandardVisitor visitor, BrygParser.VariableExpressionContext ctx, AccessMode mode) {
        super (visitor);
        this.mode = mode;
        setLine (ctx.getStart ().getLine ());

        String variableName = IdHelper.idToString (ctx.variable ().id ());
        variable = visitor.getCurrentScope ().getVariable (variableName);
        if (variable == null) {
            throw new BrygJitException ("Variable " + variableName + " not found!", getLine ());
        }

        setType (variable.getType ());
    }

    public VariableExpression (StandardVisitor visitor, Variable variable, AccessMode mode, int line) {
        super (visitor);
        this.mode = mode;
        setLine (line);

        this.variable = variable;
        setType (variable.getType ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();
        if (mode == AccessMode.get) {
            method.visitVarInsn (type.getAsmType ().getOpcode (ILOAD), variable.getId ());
            // -> T
        }else { /* AccessMode.set */
            method.visitVarInsn (type.getAsmType ().getOpcode (ISTORE), variable.getId ());
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
