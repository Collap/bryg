package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.FragmentInfo;
import io.collap.bryg.internal.TemplateType;
import io.collap.bryg.internal.type.Types;

import java.util.Iterator;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Add coercion to arguments.
// TODO: Accept implicit closure argument.

public class FragmentCallExpression extends Expression {

    private Expression callee;
    private FragmentInfo fragment;
    private List<ArgumentExpression> arguments;

    private CompiledVariable writerVariable;

    public FragmentCallExpression(CompilationContext compilationContext, int line, Expression callee,
                                  FragmentInfo fragment, List<ArgumentExpression> arguments) {
        super(compilationContext, line);
        setType(Types.fromClass(Void.TYPE));

        this.callee = callee;
        this.fragment = fragment;
        this.arguments = arguments;
        validateCallee();
        reorderAndValidateArguments();

        writerVariable = ((FragmentScope) compilationContext.getFunctionScope()).getWriterVariable();
    }

    private void validateCallee() {
        // The callee should be a unit.
        if (!callee.getType().isUnitType()) {
            throw new BrygJitException("The callee object for a fragment call must be a template or closure.", getLine());
        }
    }

    private void reorderAndValidateArguments() {
        List<ParameterInfo> parameters = fragment.getParameters();
        if (arguments.size() < parameters.size()) {
            throw new BrygJitException("Not enough arguments. " + parameters.size()
                    + " expected, got " + arguments.size() + ".", getLine());
        }

        arguments = FunctionUtil.reorderArgumentList(compilationContext, getLine(), arguments, parameters);

        // Check whether argument types match the parameter types.
        Iterator<ArgumentExpression> argumentIterator = arguments.iterator();
        Iterator<ParameterInfo> parameterIterator = parameters.iterator();
        while (argumentIterator.hasNext() && parameterIterator.hasNext()) {
            ArgumentExpression argument = argumentIterator.next();
            ParameterInfo parameter = parameterIterator.next();
            // TODO: Perform coercion.
            if (!argument.getType().similarTo(parameter.getType())) {
                throw new BrygJitException("Argument and parameter types are not compatible," +
                        " coercion is currently disabled. Expected: " + parameter.getType()
                        + ", but got: " + argument.getType() + ".", getLine());
            }
        }
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        callee.compile();
        // -> T extends Unit

        writerVariable.compile(compilationContext, VariableUsageInfo.withGetMode());
        // -> Writer

        compileArguments();
        // -> a0, a1, a2, ...

        // Add template to the list of referenced templates.
        // TODO: IMPORTANT I feel like this should NOT be done here, but instead when a template type is resolved.
        if (callee.getType() instanceof TemplateType) {
            compilationContext.getUnitType().getParentTemplateType().getCompilationData()
                    .getReferencedTemplates().add((TemplateType) callee.getType());
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, callee.getType().getInternalName(), fragment.getDirectName(),
                fragment.getDesc(), false);
    }

    private void compileArguments() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        // TODO: Implement predicates.
        for (ArgumentExpression argument : arguments) {
            argument.compile();
            // -> T of a_i
        }
    }

}
