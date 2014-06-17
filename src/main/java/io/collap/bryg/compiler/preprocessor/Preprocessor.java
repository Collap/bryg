package io.collap.bryg.compiler.preprocessor;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * The Preprocessor adds braces around indentation-specified blocks.
 * Note: The writer is not closed.
 */
public class Preprocessor {

    private final String source;
    private Writer writer;
    private int index;
    private boolean prettyPrint;

    public Preprocessor (String source, Writer writer, boolean prettyPrint) {
        this.source = source;
        this.writer = writer;
        this.prettyPrint = prettyPrint;
        this.index = 0;
    }

    public void process () throws IOException {
        Stack<Indent> indentationStack = new Stack<> ();
        indentationStack.add (new Indent (0));

        int sourceLength = source.length ();
        for (; index < sourceLength; goToNextLine ()) {
            int semanticLineEnd = getSemanticLineEnd ();
            int indentColumn = countIndentColumn (index);
            int indentColumnIndex = index + indentColumn;
            if (semanticLineEnd < indentColumnIndex) {
                continue;
            }

            Indent lastIndent =  indentationStack.peek ();
            if (lastIndent.getColumn () < indentColumn) {
                indentationStack.push (new Indent (indentColumn));
                if (prettyPrint) {
                    for (int ind = 0; ind < lastIndent.getColumn (); ++ind) {
                        writer.write (" ");
                    }
                    writer.write ("{\n");
                }else {
                    writer.write ("{");
                }
            }else {
                closeIndents (indentationStack, indentColumn);
            }

            writer.write (source.substring (index, semanticLineEnd + 1));
            writer.write ("\n");
        }

        closeIndents (indentationStack, 0);
    }

    private void goToNextLine () {
        index = getLineEnd () + 2;
    }

    private void closeIndents (Stack<Indent> indentationStack, int currentIndent) throws IOException {
        Indent lastIndent = indentationStack.peek ();
        while (lastIndent.getColumn () > currentIndent) {
            indentationStack.pop ();
            lastIndent = indentationStack.peek ();
            if (prettyPrint) {
                for (int ind = 0; ind < lastIndent.getColumn (); ++ind) {
                    writer.write (" ");
                }
                writer.write ("}\n");
            }else {
                writer.write ("}");
            }
        }
    }

    private boolean isLineEmpty () {
        int indentColumn = countIndentColumn (index);
        int firstChar = index + indentColumn;
        int sourceLength = source.length ();
        if (sourceLength > firstChar) {
            /* Possible comment. */
            if (sourceLength > firstChar + 1 && source.charAt (firstChar) == '/' && source.charAt (firstChar + 1) == '/') {
                return true;
            }else {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds the semantic end of the line, which is any character standing before one of these:
     *   - The beginning of a comment.
     *   - A newline character.
     *   - The end of the file.
     */
    private int getSemanticLineEnd () {
        int lineEnd = getLineEnd ();
        for (int i = index; i <= lineEnd; ) {
            i = source.indexOf ('/', i);
            if (i == -1 || i >= lineEnd) {
                return lineEnd;
            }else if (source.charAt (i + 1) == '/') {
                return i - 1;
            }
            ++i;
        }

        return lineEnd;
    }

    /**
     * Returns the last character in the line, excluding newline.
     */
    private int getLineEnd () {
        // TODO: Currently only accepts Unix-style newlines.
        int nextNewline = source.indexOf ('\n', index);
        if (nextNewline == -1) {
            return source.length () - 1;
        }

        return nextNewline - 1;
    }

    private int getIndentColumnOfNextLine () {
        final int nextNewline = source.indexOf ('\n', index);
        if (nextNewline >= 0) {
            int lineStart = nextNewline + 1;
            return countIndentColumn (lineStart);
        }

        return 0;
    }

    /**
     * Counts the current indent column of the line.
     */
    private int countIndentColumn (int lineStart) {
        int nextNonWhitespace = lineStart;
        final int sourceLength = source.length ();
        for (; nextNonWhitespace < sourceLength; ++nextNonWhitespace) {
            if (source.charAt (nextNonWhitespace) != ' ') {
                break;
            }
        }

        return nextNonWhitespace - lineStart;
    }

}
