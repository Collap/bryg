package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.InstanceVariable;
import io.collap.bryg.compiler.scope.LocalVariable;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private Variable variable;
    private AccessMode mode;
    private Expression rvalue;

    public VariableExpression (Context context, BrygParser.VariableExpressionContext ctx, AccessMode mode) {
        this (context, ctx, mode, null);
    }

    public VariableExpression (Context context, BrygParser.VariableExpressionContext ctx, AccessMode mode,
                               Expression rvalue) {
        super (context);
        this.mode = mode;
        setLine (ctx.getStart ().getLine ());
        setRValue (rvalue);

        String variableName = IdUtil.idToString (ctx.variable ().id ());
        variable = context.getCurrentScope ().getVariable (variableName);
        if (variable == null) {
            throw new BrygJitException ("Variable " + variableName + " not found!", getLine ());
        }

        setType (variable.getType ());
        variable.incrementUses ();
    }

    public VariableExpression (Context context, int line, Variable variable, AccessMode mode) {
        this (context, line, variable, mode, null);
    }

    public VariableExpression (Context context, int line, Variable variable, AccessMode mode, Expression rvalue) {
        super (context);
        this.mode = mode;
        setLine (line);
        setRValue (rvalue);

        this.variable = variable;
        setType (variable.getType ());
        variable.incrementUses ();
    }

    @Override
    public void compile () {
        if (variable instanceof LocalVariable) {
            compileLocalVariable (((LocalVariable) variable));
        }else if (variable instanceof InstanceVariable) {
            compileInstanceVariable (((InstanceVariable) variable));
        }else {
            throw new BrygJitException ("Unrecognized variable kind: " + variable.getType (), getLine ());
        }
    }

    private void compileLocalVariable (LocalVariable variable) {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (mode == AccessMode.get) {
            mv.visitVarInsn (type.getOpcode (ILOAD), variable.getId ());
            // -> T
        }else { /* AccessMode.set */
            rvalue.compile ();
            mv.visitVarInsn (type.getOpcode (ISTORE), variable.getId ());
            // T ->
        }
    }

    private void compileInstanceVariable (InstanceVariable variable) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        // TODO: Uses is incremented here at "compile time".
        new VariableExpression (context, getLine (), context.getHighestLocalScope ().getVariable ("this"),
                AccessMode.get).compile ();
        // -> this

        if (mode == AccessMode.get) {
            mv.visitFieldInsn (GETFIELD, context.getUnitType ().getInternalName (), variable.getName (),
                    variable.getType ().getDescriptor ());
            // this -> T
        }else { /* AccessMode.set */
            rvalue.compile ();
            // -> T

            mv.visitFieldInsn (PUTFIELD, context.getUnitType ().getInternalName (), variable.getName (),
                    variable.getType ().getDescriptor ());
            // this, T ->
        }
    }

    public Variable getVariable () {
        return variable;
    }

    public AccessMode getMode () {
        return mode;
    }

    public void setRValue (Expression rvalue) {
        if (rvalue == null) return;

        if (mode == AccessMode.get) {
            throw new BrygJitException ("A new value can only be set with AccessMode.set", getLine ());
        }

        this.rvalue = rvalue;
    }

}
