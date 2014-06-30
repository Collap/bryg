package io.collap.bryg.compiler.preprocessor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * The Preprocessor adds braces around indentation-specified blocks.
 * Note: The writer is not closed.
 */
public class Preprocessor {

    private final String source;
    private Writer writer;
    private int index = 0;
    private boolean prettyPrint;

    /**
     * A map that points from the preprocessed source line to the actual source line.
     */
    private Map<Integer, Integer> lineToSourceLineMap = new HashMap<> ();
    private int prepLine = 1;
    private int sourceLine = 1;

    public Preprocessor (String source, Writer writer, boolean prettyPrint) {
        this.source = source;
        this.writer = writer;
        this.prettyPrint = prettyPrint;
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
                        writer.write (' ');
                    }
                }
                writer.write ("{\n");
                prepLine += 1;
            }else {
                closeIndents (indentationStack, indentColumn);
            }

            int writeStartIndex;
            if (prettyPrint) {
                writeStartIndex = index;
            }else {
                writeStartIndex = indentColumnIndex;
            }

            writer.write (source.substring (writeStartIndex, semanticLineEnd + 1));
            lineToSourceLineMap.put (prepLine, sourceLine);

            writer.write ("\n");
            prepLine += 1;
        }

        closeIndents (indentationStack, 0);
    }

    private void goToNextLine () {
        index = getLineEnd () + 2;
        sourceLine += 1;
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
            }
            writer.write ("}\n");
            prepLine += 1;
        }
    }

    /**
     * Finds the semantic end of the line, which is any character standing before one of these:
     *   - The beginning of a comment.
     *   - A newline character.
     *   - The end of the file.
     */
    private int getSemanticLineEnd () {
        int lineEnd = getLineEnd ();
        boolean inString = false;
        char stringChar = '\0';
        char lastChar = '\0';
        for (int i = index; i <= lineEnd; ++i) {
            char currentChar = source.charAt (i);
            if (lastChar != '\\' && (currentChar == '"' || currentChar == '`')) {
                if (!inString) {
                    inString = true;
                    stringChar = currentChar;
                }else if (currentChar == stringChar) {
                    inString = false;
                    stringChar = '\0';
                }
                System.out.println (inString + " " + currentChar + " {" + source.substring (0, i + 1));
            }else if (!inString && currentChar == '/') {
                if (source.charAt (i + 1) == '/') {
                    return i - 1;
                }
            }else if (currentChar == '\n') {
                return lineEnd;
            }

            lastChar = currentChar;
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

    public Map<Integer, Integer> getLineToSourceLineMap () {
        return lineToSourceLineMap;
    }

}
