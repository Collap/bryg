package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.RenderVisitor;

public class TextCommandNode extends Node {

    private String text;

    public TextCommandNode (RenderVisitor visitor, String text) {
        super (visitor);
        this.text = text;
    }

    @Override
    public void compile () {
        /* Trim quotes. */
        if (text.charAt (0) == '`') {
            text = text.substring (1);
        }
        int lastCharIndex = text.length () - 1;
        if (text.charAt (lastCharIndex) == '`') {
            text = text.substring (0, lastCharIndex);
        }

        visitor.getMethod ().writeConstantString (text);
    }

}
