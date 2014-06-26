package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.expression.Variable;

import static org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private Variable variable;

    public VariableExpression (StandardVisitor visitor, Variable variable) {
        super (visitor);
        this.variable = variable;
        setType (variable.getType ());
    }

    @Override
    public void compile () {
        int opcode = type.getAsmType ().getOpcode (ILOAD);
        visitor.getMethod ().visitVarInsn (opcode, variable.getId ());
        // -> type
    }

}
