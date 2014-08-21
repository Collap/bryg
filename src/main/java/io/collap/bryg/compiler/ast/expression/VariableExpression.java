package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.expression.Variable;

import static org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private Variable variable;
    private AccessMode mode;

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
