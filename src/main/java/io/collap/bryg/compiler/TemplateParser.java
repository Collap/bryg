package io.collap.bryg.compiler;

import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.compiler.visitor.PrintTreeVisitor;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.TemplateFragmentCompileInfo;
import io.collap.bryg.template.TemplateFragmentInfo;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.unit.FragmentInfo;
import io.collap.bryg.unit.ParameterInfo;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

        /* The render fragment only has general parameters. */
        ArrayList<BrygParser.InDeclarationContext> renderParamContexts = new ArrayList<> ();
        TemplateFragmentInfo renderFragment = new TemplateFragmentInfo ("render", parseParameters (renderParamContexts));
        TemplateFragmentCompileInfo renderCompileInfo = new TemplateFragmentCompileInfo (renderFragment,
                renderParamContexts,
                startContext.statement ());

        List<TemplateFragmentCompileInfo> compileInfos = new ArrayList<> ();
        compileInfos.add (renderCompileInfo);

        List<BrygParser.FragmentFunctionContext> fragmentContexts = startContext.fragmentFunction ();
        for (BrygParser.FragmentFunctionContext fragCtx : fragmentContexts) {
            compileInfos.add (parseFragment (fragCtx));
        }

        TemplateType templateType = new TemplateType (className,
                parseParameters (startContext.inDeclaration ()), /* General parameters. */
                compileInfos,
                startContext.inDeclaration ()
        );

        System.out.println ("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                " took " + parseTime + "s.");

        return templateType;
    }

    private TemplateFragmentCompileInfo parseFragment (BrygParser.FragmentFunctionContext ctx) {
        BrygParser.FragmentBlockContext fragBlockCtx = ctx.fragmentBlock ();
        List<ParameterInfo> parameters = parseParameters (fragBlockCtx.inDeclaration ());
        TemplateFragmentInfo fragmentInfo = new TemplateFragmentInfo (IdUtil.idToString (ctx.id ()), parameters);
        return new TemplateFragmentCompileInfo (fragmentInfo, fragBlockCtx.inDeclaration (), fragBlockCtx.statement ());
    }

    private List<ParameterInfo> parseParameters (List<BrygParser.InDeclarationContext> ctxs) {
        List<ParameterInfo> parameters = new ArrayList<> ();
        for (BrygParser.InDeclarationContext ctx : ctxs) {
            String name = IdUtil.idToString (ctx.id ());
            Type type = new TypeInterpreter (environment.getClassResolver ()).interpretType (ctx.type ());
            boolean optional = ctx.qualifier.getType () == BrygLexer.OPT;
            parameters.add (new ParameterInfo (name, type, optional));
        }
        return parameters;
    }

}
