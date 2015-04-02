package io.collap.bryg.internal.module.html;

import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.module.Function;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;

import java.util.Collections;
import java.util.List;

public class HtmlFunction extends Function {

    protected String tag;
    protected String[] validAttributes;
    protected boolean hasContent;

    /**
     * @param validAttributes These lists <b>must</b> be sorted alphabetically (A to Z).
     */
    protected HtmlFunction(String tag, String[] validAttributes, boolean hasContent) {
        this.tag = tag;
        this.validAttributes = validAttributes;
        this.hasContent = hasContent;
    }

    @Override
    public final void compile(Context context, FunctionCallExpression call) {
        BrygMethodVisitor mv = context.getMethodVisitor();

        // Write opening tag.
        mv.writeConstantString ("<" + tag);
        new AttributeCompiler(mv, call.getArgumentExpressions (), validAttributes).compile ();
        mv.writeConstantString (">");

        // Write content and closing tag.
        if (hasContent) {
            call.getStatementOrBlock().compile();
            mv.writeConstantString("</" + tag + ">");
        }
    }

    @Override
    public Type getResultType() {
        return Types.fromClass(Void.TYPE);
    }

    @Override
    public List<Type> getParameters() {
        return Collections.emptyList();
    }

}