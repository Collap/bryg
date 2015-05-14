package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.Closure;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
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
        addWriterArgument();
        reorderAndValidateArguments();
    }

    private void validateCallee() {
        // The callee should be a unit.
        if (!callee.getType().isUnitType()) {
            throw new BrygJitException("The callee object for a fragment call must be a template or closure.", getLine());
        }
    }

    private void addWriterArgument() {
        arguments.add(0, new ArgumentExpression(
                compilationContext, getLine(),
                new VariableExpression(
                        compilationContext, getLine(),
                        ((FragmentScope) compilationContext.getFunctionScope()).getWriterVariable(),
                        VariableUsageInfo.withGetMode()
                ),
                "writer", null
        ));
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
            if (!argument.getType().similarTo(parameter.getType())) {
                // Try coercion.
                argument.setExpression(CoercionUtil.applyUnaryCoercion(
                        compilationContext, argument.getExpression(), parameter.getType()
                ));
            }
        }
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        callee.compile();
        // -> T extends Unit

        compileArguments();
        // -> Writer, a0, a1, a2, ...

        // Add template to the list of referenced templates.
        // TODO: IMPORTANT I feel like this should NOT be done here, but instead when a template type is resolved.
        if (callee.getType() instanceof TemplateType) {
            compilationContext.getUnitType().getParentTemplateType().getCompilationData()
                    .getReferencedTemplates().add((TemplateType) callee.getType());
        }

        boolean isInterface = callee.getType().isInterface();
        mv.visitMethodInsn(isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
                callee.getType().getInternalName(), fragment.getDirectName(),
                fragment.getDesc(), isInterface);
    }

    private void compileArguments() {
        // TODO: Implement predicates.
        for (ArgumentExpression argument : arguments) {
            argument.compile();
            // -> T of a_i
        }
    }

}
