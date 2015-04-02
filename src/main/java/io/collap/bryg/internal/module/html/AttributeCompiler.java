package io.collap.bryg.internal.module.html;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.util.StringBuilderCompileHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AttributeCompiler {

    // TODO: Some of these arguments do not make sense in every attribute, according to the spec.
    // See: http://www.w3.org/TR/html5/dom.html#global-attributes
    // The validator might be extended with warnings that are thrown when one uses a supposedly valid
    // tag on an element where it makes no sense.

    /**
     * Notes:
     * - The data-* attributes are also global attributes, but already accounted for in the attribute compiler.
     * - The xmlns "attribute" is not supported in this version of bryg!
     */
    public static final String[] validGlobalAttributes = new String[]{
            "accesskey", "class", "contenteditable", "dir", "draggable",
            "dropzone", "hidden", "id", "lang",
            "onabort", "onblur", "oncancel", "oncanplay", "oncanplaythrough",
            "onchange", "onclick", "oncuechange", "ondblclick", "ondrag",
            "ondragend", "ondragenter", "ondragexit", "ondragleave", "ondragover",
            "ondragstart", "ondrop", "ondurationchange", "onemptied", "onended",
            "onerror", "onfocus", "oninput", "oninvalid", "onkeydown", "onkeypress",
            "onkeyup", "onload", "onloadeddata", "onloadedmetadata", "onloadstart",
            "onmousedown", "onmouseenter", "onmouseleave", "onmousemove",
            "onmouseout", "onmouseover", "onmouseup", "onmousewheel",
            "onpause", "onplay", "onplaying", "onprogress", "onratechange",
            "onreset", "onresize", "onscroll", "onseeked", "onseeking",
            "onselect", "onshow", "onstalled", "onsubmit", "onsuspend",
            "ontimeupdate", "ontoggle", "onvolumechange", "onwaiting",
            "spellcheck", "style", "tabindex", "title", "translate"
    };

    private class ArgumentComparator implements Comparator<ArgumentExpression> {
        @Override
        public int compare(ArgumentExpression o1, ArgumentExpression o2) {
            int result = 0;
            if (o1.isConstant() && o1.getPredicate() == null) result -= 1;
            if (o2.isConstant() && o2.getPredicate() == null) result += 1;
            return result;
        }
    }

    private BrygMethodVisitor mv;
    private List<ArgumentExpression> arguments;
    private String[] validAttributes;

    public AttributeCompiler(BrygMethodVisitor mv, List<ArgumentExpression> arguments, String[] validAttributes) {
        this.mv = mv;
        this.arguments = arguments;
        this.validAttributes = validAttributes;

        // Sort arguments by constant and non-constant to minimize write calls.
        Collections.sort(arguments, new ArgumentComparator());
    }

    public void compile() {
        for (ArgumentExpression attribute : arguments) {
            String name = attribute.getName();

            if (name == null) {
                throw new BrygJitException("Argument to a HTML function call is unnamed.", attribute.getLine());
            }

            boolean valid = name.startsWith("data-")
                    || Arrays.binarySearch(validAttributes, name) >= 0
                    || Arrays.binarySearch(validGlobalAttributes, name) >= 0;

            if (!valid) {
                // TODO: Add notice for which tag said attribute is not defined. (Fix with Improved Error Handling)
                System.out.println("Warning: The attribute " + name + " is not a valid HTML5 attribute! Line: " + attribute.getLine());
            }

            Label nextFalseLabel = attribute.compilePredicate();

            Object constantValue = attribute.getConstantValue();
            boolean isEmpty = attribute.isConstant() &&
                    (constantValue instanceof String
                            && ((String) constantValue).isEmpty()
                            || constantValue == null); /* Ignore a 'null' constant. */

            mv.writeConstantString(" " + name);

            if (!isEmpty) {
                mv.writeConstantString("=\"");
                if (constantValue != null) {
                    mv.writeConstantString(constantValue.toString());
                } else {
                    mv.loadWriter();
                    // -> Writer

                    if (attribute.getType().similarTo(String.class)) {
                        attribute.compile();
                        // -> value
                    } else {
                        StringBuilderCompileHelper stringBuilder = new StringBuilderCompileHelper(mv);
                        stringBuilder.compileNew();
                        stringBuilder.compileAppend(attribute); // Note: The expression is compiled here!
                        stringBuilder.compileToString();
                        // -> String
                    }

                    mv.writeString();
                    // Writer, value ->
                }

                mv.writeConstantString("\"");
            }

            if (nextFalseLabel != null) {
                mv.visitLabel(nextFalseLabel);
            }
        }
    }

}
