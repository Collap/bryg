package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.VariableInfo;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.scope.CompiledVariable;
import io.collap.bryg.internal.scope.InstanceVariable;
import io.collap.bryg.internal.scope.LocalVariable;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.*;

public class VariableExpression extends Expression {

    private CompiledVariable variable;
    private AccessMode mode;
    private Expression rightExpression;

    public VariableExpression (Context context, BrygParser.VariableExpressionContext ctx, AccessMode mode) {
        this (context, ctx, mode, null);
    }

    public VariableExpression (Context context, BrygParser.VariableExpressionContext ctx, AccessMode mode,
                               Expression rightExpression) {
        super (context);
        this.mode = mode;
        setLine (ctx.getStart ().getLine ());
        setRValue (rightExpression);

        String variableName = IdUtil.idToString (ctx.variable ().id ());
        variable = context.getCurrentScope ().getVariable (variableName);
        if (variable == null) {
            throw new BrygJitException ("Variable " + variableName + " not found!", getLine ());
        }

        setType(variable.getType());
    }

    public VariableExpression (Context context, int line, CompiledVariable variable, AccessMode mode) {
        this (context, line, variable, mode, null);
    }

    public VariableExpression (Context context, int line, CompiledVariable variable, AccessMode mode, Expression rightExpression) {
        super (context);
        this.mode = mode;
        setLine (line);
        setRValue (rightExpression);

        this.variable = variable;
        setType(variable.getType());
    }

    @Override
    public void compile () {
        variable.compile(context, this);
    }

    private void compileLocalVariable (LocalVariable variable) {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (mode == AccessMode.get) {
            mv.visitVarInsn (type.getOpcode (ILOAD), variable.getId ());
            // -> T
        }else { /* AccessMode.set */
            rightExpression.compile();
            mv.visitVarInsn (type.getOpcode (ISTORE), variable.getId ());
            // T ->
        }
    }

    private void compileInstanceVariable (InstanceVariable variable) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        // TODO: Uses is incremented here at "compile time".
        new VariableExpression (context, getLine (), context.getHighestLocalScope().getVariable ("this"),
                AccessMode.get).compile ();
        // -> this

        if (mode == AccessMode.get) {
            mv.visitFieldInsn (GETFIELD, context.getUnitType ().getInternalName (), variable.getName (),
                    variable.getType ().getDescriptor ());
            // this -> T
        }else { /* AccessMode.set */
            rightExpression.compile();
            // -> T

            mv.visitFieldInsn (PUTFIELD, context.getUnitType ().getInternalName (), variable.getName (),
                    variable.getType ().getDescriptor ());
            // this, T ->
        }
    }

    public VariableInfo getVariable () {
        return variable;
    }

    public AccessMode getMode () {
        return mode;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public void setRValue (Expression rvalue) {
        if (rvalue == null) return;

        if (mode == AccessMode.get) {
            throw new BrygJitException ("A new value can only be set with AccessMode.set", getLine ());
        }

        this.rightExpression = rvalue;
    }

}
