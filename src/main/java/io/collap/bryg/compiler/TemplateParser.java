package io.collap.bryg.compiler;

import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.compiler.visitor.PrintTreeVisitor;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.unit.ParameterInfo;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TemplateParser implements Parser<TemplateType> {

    private StandardEnvironment environment;
    private String className;
    private String source;

    public TemplateParser (StandardEnvironment environment, String className, String source) {
        this.environment = environment;
        this.className = className;
        this.source = source;
    }

    @Override
    public TemplateType parse () {
        long parseStart = System.nanoTime ();

        boolean usedSLL = false;
        BrygParser.StartContext startContext = null;
        InputStream stream = new ByteArrayInputStream (source.getBytes ());
        try {
            BrygLexer lexer = new BrygLexer (new ANTLRInputStream (stream));
            CommonTokenStream tokenStream = new CommonTokenStream (lexer);

            if (environment.getConfiguration ().shouldPrintTokens ()) {
                while (true) {
                    Token token = tokenStream.LT (1);
                    if (token.getType () == -1 /* EOF */) break;
                    else {
                        System.out.println (token);
                    }
                    tokenStream.consume ();
                }

                System.out.println ();
                tokenStream.reset ();
            }

            /* Try with SLL(*). */
            BrygParser parser = new BrygParser (tokenStream);
            parser.getInterpreter ().setPredictionMode (PredictionMode.SLL);
            parser.removeErrorListeners ();
            parser.setErrorHandler (new BailErrorStrategy ());
            try {
                startContext = parser.start ();
                usedSLL = true;
            } catch (ParseCancellationException ex) {
                if (ex.getCause () instanceof RecognitionException) {
                    /* Try again with LL(*). */
                    tokenStream.reset ();
                    parser.addErrorListener (ConsoleErrorListener.INSTANCE);
                    parser.addErrorListener (new DiagnosticErrorListener ());
                    parser.setErrorHandler(new DefaultErrorStrategy());

                    parser.getInterpreter ().setPredictionMode (PredictionMode.LL);

                    startContext = parser.start ();
                }
            }
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle. (Fix with Improved Error Handling)
        }

        if (startContext == null) return null;

        if (environment.getConfiguration ().shouldPrintParseTree ()) {
            PrintTreeVisitor printTreeVisitor = new PrintTreeVisitor ();
            printTreeVisitor.visit (startContext);
            System.out.println ();
        }

        double parseTime = (System.nanoTime () - parseStart) / 1.0e9;

        TemplateType templateType = new TemplateType (className, startContext);
        parseParameters (templateType, startContext);

        System.out.println ("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                " took " + parseTime + "s.");

        return templateType;
    }

    private void parseParameters (TemplateType templateType, BrygParser.StartContext startContext) {
        List<BrygParser.InDeclarationContext> contexts = startContext.inDeclaration ();
        for (BrygParser.InDeclarationContext context : contexts) {
            String name = IdUtil.idToString (context.id ());
            Type type = new TypeInterpreter (environment.getClassResolver ()).interpretType (context.type ());
            boolean optional = context.qualifier.getType () == BrygLexer.OPT;
            templateType.addParameter (new ParameterInfo (name, type, optional));
        }
    }


}
