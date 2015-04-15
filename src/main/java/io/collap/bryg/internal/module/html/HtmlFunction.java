package io.collap.bryg.internal.module.html;

import io.collap.bryg.internal.MemberFunctionCallInfo;
import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.compiler.ast.expression.MemberFunctionCallExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.module.MemberFunction;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;

import java.util.Collections;
import java.util.List;

public class HtmlFunction extends MemberFunction {

    protected String[] validAttributes;
    protected boolean hasContent;

    /**
     * @param validAttributes These lists <b>must</b> be sorted alphabetically (A to Z).
     */
    protected HtmlFunction(String name, String[] validAttributes, boolean hasContent) {
        super(name);
        this.validAttributes = validAttributes;
        this.hasContent = hasContent;
    }

    @Override
    public final void compile(CompilationContext compilationContext, MemberFunctionCallInfo callInfo) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        // Write opening tag.
        mv.writeConstantString ("<" + name);
        new AttributeCompiler(mv, callInfo.getArgumentExpressions (), validAttributes).compile();
        mv.writeConstantString (">");

        // Write content and closing tag.
        if (hasContent) {
            if (callInfo.getStatementOrBlock() != null) {
                callInfo.getStatementOrBlock().compile();
            }
            mv.writeConstantString("</" + name + ">");
        }
    }

    @Override
    public Type getResultType() {
        return Types.fromClass(Void.TYPE);
    }

    /**
     * Although the HTML function certainly does have arguments, it is compiled in a way that
     * makes it irrelevant.
     */
    @Override
    public List<ParameterInfo> getParameters() {
        return Collections.emptyList();
    }

}