package io.collap.bryg.internal.compiler;

import io.collap.bryg.CompilationException;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateParser implements Parser<TemplateType> {

    private StandardEnvironment environment;
    private String className;
    private String source;

    public TemplateParser(StandardEnvironment environment, String className, String source) {
        this.environment = environment;
        this.className = className;
        this.source = source;
    }

    @Override
    public TemplateType parse() {
        @Nullable BrygParser.StartContext startContext = createParseTree();
        if (startContext == null) {
            throw new CompilationException("Could not build a parse tree from the supplied source!");
        }

        if (environment.getDebugConfiguration().shouldPrintParseTree()) {
            PrintTreeVisitor printTreeVisitor = new PrintTreeVisitor();
            printTreeVisitor.visit(startContext);
            System.out.println();
        }

        List<BrygParser.FragmentFunctionContext> fragmentContexts = startContext.fragmentFunction();
        // TODO: Use parallel stream here?
        List<FragmentCompileInfo> compileInfos = fragmentContexts.stream().map(this::parseFragment).collect(Collectors.toList());

        return new TemplateType(className, new TemplateCompilationData(compileInfos,
                startContext.fieldDeclaration(), new HashSet<>()), environment.getClassResolver());
    }

    private @Nullable BrygParser.StartContext createParseTree() {
        @Nullable BrygParser.StartContext startContext = null;

        InputStream stream = new ByteArrayInputStream(source.getBytes());
        try {
            long parseStart = System.nanoTime();

            BrygLexer lexer = new BrygLexer(new ANTLRInputStream(stream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);

            if (environment.getDebugConfiguration().shouldPrintTokens()) {
                while (true) {
                    Token token = tokenStream.LT(1);
                    System.out.println(token);
                    if (token.getType() == -1 /* EOF */) {
                        break;
                    }
                    tokenStream.consume();
                }

                System.out.println();
                tokenStream.reset();
            }

            /* Try with SLL(*). */
            BrygParser parser = new BrygParser(tokenStream);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.removeErrorListeners();
            parser.setErrorHandler(new BailErrorStrategy());
            boolean usedSLL = false;
            try {
                startContext = parser.start();
                usedSLL = true;
            } catch (ParseCancellationException ex) { // Occurs when the SLL(*) parser did not find a result.
                if (ex.getCause() instanceof RecognitionException) {
                    /* Try again with LL(*). */
                    tokenStream.reset();
                    parser.addErrorListener(ConsoleErrorListener.INSTANCE);
                    parser.addErrorListener(new DiagnosticErrorListener());
                    parser.setErrorHandler(new DefaultErrorStrategy());

                    parser.getInterpreter().setPredictionMode(PredictionMode.LL);

                    startContext = parser.start();
                }
            }

            double parseTime = (System.nanoTime() - parseStart) / 1.0e9;
            System.out.println("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                    " took " + parseTime + "s.");
        } catch (IOException e) {
            throw new CompilationException("IO exception occurred during ANTLR parsing.", e);
        }

        return startContext;
    }

    private FragmentCompileInfo parseFragment(BrygParser.FragmentFunctionContext ctx) {
        BrygParser.FragmentBlockContext fragBlockCtx = ctx.fragmentBlock();
        List<ParameterInfo> parameters = FunctionUtil.parseParameterList(environment,
                ctx.parameterList().parameterDeclaration());
        String name;
        if (ctx.id() != null) {
            name = IdUtil.idToString(ctx.id());
        } else {
            name = UnitType.DEFAULT_FRAGMENT_NAME;
        }
        boolean isDefault = ctx.DEFAULT() != null;

        // The function's name must not be "default", while it is not default. (For example via `default`)
        if (!isDefault && name.equals(UnitType.DEFAULT_FRAGMENT_NAME)) {
            throw new CompilationException("The fragment " + name + " is named 'default' but is not default.");
        }

        return new FragmentCompileInfo(name, isDefault, parameters, fragBlockCtx.statement());
    }

}
