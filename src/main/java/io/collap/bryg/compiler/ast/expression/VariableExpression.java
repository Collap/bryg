package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.expression.Type;
import io.collap.bryg.compiler.expression.Variable;

import static org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private Variable variable;

    public VariableExpression (RenderVisitor visitor, Variable variable) {
        super (visitor);
        this.variable = variable;
        setType (variable.getType ());
    }

    @Override
    public void compile () {
        int opcode = NOP;
        if (type instanceof ClassType) {
            opcode = ALOAD;
        }else { /* PrimitiveType. */
            switch ((PrimitiveType) type) {
                case _boolean:
                case _char:
                case _byte:
                case _short:
                case _int:
                    opcode = ILOAD;
                    break;

                case _long:
                    opcode = LLOAD;
                    break;

                case _float:
                    opcode = FLOAD;
                    break;

                case _double:
                    opcode = DLOAD;
                    break;
            }
        }

        if (opcode != NOP) {
            visitor.getMethod ().visitVarInsn (opcode, variable.getId ());
        }else {
            System.out.println ("Load operation for type " + type + " could not be found!");
            return;
        }
    }

}
