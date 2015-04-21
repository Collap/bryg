package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateInstantiationExpression extends Expression {

    private List<ArgumentExpression> arguments;

    public TemplateInstantiationExpression(CompilationContext compilationContext,
                                           BrygParser.TemplateInstantiationContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        String name = IdUtil.templateIdToString(compilationContext, ctx.templateId());
        @Nullable TemplateType templateType = compilationContext.getEnvironment().getTemplateType(name);
        if (templateType == null) {
            throw new BrygJitException("Template type " + name + " not found!", getLine());
        }
        setType(templateType);

        arguments = FunctionUtil.parseArgumentList(compilationContext, ctx.argumentList());

        // Add __environment argument. The argument is named to ensure that the reordering does
        // recognize this argument as the first argument in all cases.
        CompiledVariable environmentVariable = compilationContext.getUnitScope().getEnvironmentField();
        arguments.add(0, new ArgumentExpression(compilationContext, getLine(),
                new VariableExpression(
                        compilationContext, getLine(),
                        environmentVariable, VariableUsageInfo.withGetMode()
                ), StandardUnit.ENVIRONMENT_FIELD_NAME, null));

        // Reorder arguments to allow named and unnamed arguments in a single call.
        arguments = FunctionUtil.reorderArgumentList(compilationContext, getLine(), arguments,
                templateType.getConstructorInfo().getParameters());

        // TODO: Check parameter and argument types and apply coercion.
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();
        TemplateType templateType = (TemplateType) type;

        // Create new template instance.
        mv.visitTypeInsn(NEW, templateType.getInternalName());
        // -> T extends StandardTemplate

        mv.visitInsn(DUP);
        // T -> T, T

        // Load arguments.
        // Get environment as first argument.
        compilationContext.getFragmentScope().getThisVariable().compile(compilationContext,
                VariableUsageInfo.withGetMode());
        // -> StandardUnit

        mv.visitFieldInsn(GETFIELD, Types.fromClass(StandardUnit.class).getInternalName(),
                StandardUnit.ENVIRONMENT_FIELD_NAME, Types.fromClass(StandardEnvironment.class).getDescriptor());
        // StandardUnit -> StandardEnvironment

        for (ArgumentExpression argument : arguments) {
            argument.compile();
        }
        // -> A1, A2, A3, ...

        mv.visitMethodInsn(INVOKESPECIAL, templateType.getInternalName(), "<init>",
                templateType.getConstructorInfo().getDesc(), false);
        // T, StandardEnvironment, A1, A2, A3, ... ->

        // Result: -> T
    }

}
