package io.collap.bryg.compiler.util;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.InterpolationExpression;
import io.collap.bryg.compiler.ast.expression.literal.StringLiteralExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InterpolationUtil {

    public static Expression compileString (Context context, String value, int line) {
        List<Expression> expressions = new ArrayList<> ();

        /* Search for interpolation. */
        int nextInterpolation = 0;
        int afterLastInterpolation = 0;
        while (nextInterpolation < value.length ()) {
            nextInterpolation = value.indexOf ('\\', nextInterpolation);
            if (nextInterpolation >= 0) {
                String interpolationString = null;
                if (nextInterpolation > 0) {
                    char before = value.charAt (nextInterpolation - 1);
                    if (before != '\\') {
                        interpolationString = getInterpolationString (value, nextInterpolation);
                    }
                }else {
                    interpolationString = getInterpolationString (value, nextInterpolation);
                }

                if (interpolationString != null) {
                    String text = value.substring (afterLastInterpolation, nextInterpolation);
                    expressions.add (new StringLiteralExpression (context, text, line));

                    /* Add expression to the expression list. */
                    InputStream stream = new ByteArrayInputStream (interpolationString.getBytes ());
                    BrygLexer lexer;
                    try {
                        lexer = new BrygLexer (new ANTLRInputStream (stream));
                    } catch (IOException e) {
                        e.printStackTrace ();
                        throw new BrygJitException ("Interpolation source could not be read!", line);
                    }

                    CommonTokenStream tokenStream = new CommonTokenStream (lexer);
                    BrygParser parser = new BrygParser (tokenStream);
                    BrygParser.InterpolationContext interpolationContext = parser.interpolation ();

                    Expression expression = (Expression) context.getParseTreeVisitor ().visit (interpolationContext);
                    expressions.add (expression);

                    /* Go to the character after the interpolation. */
                    nextInterpolation += interpolationString.length () + 3;
                    afterLastInterpolation = nextInterpolation;
                }
            }else {
                break;
            }
        }

        if (!expressions.isEmpty ()) {
            /* Add the rest of the string as another literal. */
            if (afterLastInterpolation < value.length ()) {
                String text = value.substring (afterLastInterpolation);
                expressions.add (new StringLiteralExpression (context, text, line));
            }

            return new InterpolationExpression (context, expressions, line);
        }

        return new StringLiteralExpression (context, value, line);
    }

    /**
     * @return The source of the interpolation without the braces and backslash, or null if the
     *         string fragment is no interpolation.
     */
    private static String getInterpolationString (String value, int interpolationStart) {
        int afterIndex = interpolationStart + 1;
        if (afterIndex < value.length ()) {
            char after = value.charAt (afterIndex);
            if (after == '{') {
                // TODO: This technique dictates that no '}' are allowed inside an interpolation! (Fix in 0.3)
                int nextRBrace = value.indexOf ('}', afterIndex);
                return value.substring (afterIndex + 1, nextRBrace);
            }
        }

        return null;
    }

}
