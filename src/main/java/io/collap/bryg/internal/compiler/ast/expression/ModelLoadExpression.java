package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.Nullness;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.OperationUtil;
import io.collap.bryg.InvalidInputParameterException;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;
import static bryg.org.objectweb.asm.Opcodes.CHECKCAST;

public class ModelLoadExpression extends Expression {

    private VariableInfo target; // TODO: Allow the model load expression to take a parameter info as well, to support default values
                                 // (Or alternatively just add another (nullable) attribute for a default value)
    private CompiledVariable modelVariable;

    public ModelLoadExpression(CompilationContext compilationContext, VariableInfo target,
                               CompiledVariable modelVariable) {
        super(compilationContext, Node.UNKNOWN_LINE); // TODO: There can definitely be a line here for normal fragments.
        setType(target.getType());

        this.target = target;
        this.modelVariable = modelVariable;
    }

    @Override
    public void compile() {
        /* Get, check, cast and store variable. */
        loadVariable();
        // -> Object

        if (target.getNullness() == Nullness.notnull) {
            ifNullThrowException();
        }

        cast();
    }

    private void loadVariable() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();


        modelVariable.compile(compilationContext, VariableUsageInfo.withGetMode());
        // -> Model

        mv.visitLdcInsn(target.getName());
        // -> String

        boolean isInterface = modelVariable.getType().isInterface();
        mv.visitMethodInsn(isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
                modelVariable.getType().getInternalName(),
                "getVariable",
                TypeHelper.generateMethodDesc(
                        new Class<?>[]{String.class},
                        Object.class
                ),
                isInterface);
        // Model, String -> Object
    }

    private void ifNullThrowException() {
        OperationUtil.compileIfNullThrowException(compilationContext.getMethodVisitor(),
                Types.fromClass(InvalidInputParameterException.class),
                target.getName() + " could not be loaded!");
    }

    // TODO: Coercion here?
    private void cast() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        Type expectedType = target.getType();
        @Nullable Type wrapperType = expectedType.getWrapperType();

        if (wrapperType != null) {
            mv.visitTypeInsn(CHECKCAST, wrapperType.getInternalName());
            // Object -> T

            new UnboxingExpression(compilationContext, new DummyExpression(compilationContext, wrapperType,
                    getLine()), expectedType).compile();
            // T -> primitive
        } else {
            mv.visitTypeInsn(CHECKCAST, target.getType().getInternalName());
            // Object -> T
        }
    }

}
