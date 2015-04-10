package io.collap.bryg.internal.compiler;

import io.collap.bryg.CompilationException;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.*;
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
import java.util.ArrayList;
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
        long parseStart = System.nanoTime();

        boolean usedSLL = false;
        @Nullable BrygParser.StartContext startContext = null;
        InputStream stream = new ByteArrayInputStream(source.getBytes());
        try {
            BrygLexer lexer = new BrygLexer(new ANTLRInputStream(stream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);

            if (environment.getDebugConfiguration().shouldPrintTokens()) {
                while (true) {
                    Token token = tokenStream.LT(1);
                    if (token.getType() == -1 /* EOF */) break;
                    else {
                        System.out.println(token);
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
            try {
                startContext = parser.start();
                usedSLL = true;
            } catch (ParseCancellationException ex) {
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
        } catch (IOException e) {
            throw new CompilationException("IO exception occurred during ANTLR parsing.", e);
        }

        if (startContext == null) {
            throw new CompilationException("Could not build a parse tree from the supplied source!");
        }

        if (environment.getDebugConfiguration().shouldPrintParseTree()) {
            PrintTreeVisitor printTreeVisitor = new PrintTreeVisitor();
            printTreeVisitor.visit(startContext);
            System.out.println();
        }

        double parseTime = (System.nanoTime() - parseStart) / 1.0e9;

        List<BrygParser.FragmentFunctionContext> fragmentContexts = startContext.fragmentFunction();
        // TODO: Use parallel stream here?
        List<FragmentCompileInfo> compileInfos = fragmentContexts.stream().map(this::parseFragment).collect(Collectors.toList());

        // TODO: Parse fields.

        TemplateType templateType = new TemplateType(className, new TemplateCompilationData(compileInfos,
                startContext.inDeclaration(), new HashSet<>()));

        System.out.println("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                " took " + parseTime + "s.");

        return templateType;
    }

    private FragmentCompileInfo parseFragment(BrygParser.FragmentFunctionContext ctx) {
        BrygParser.FragmentBlockContext fragBlockCtx = ctx.fragmentBlock();
        List<ParameterInfo> parameters = parseParameters(fragBlockCtx.inDeclaration());
        return new FragmentCompileInfo(IdUtil.idToString(ctx.id()), parameters, fragBlockCtx.statement());
    }

    private List<FieldInfo> parseFields(List<BrygParser.InDeclarationContext> contexts) {
        List<FieldInfo> fields = new ArrayList<>();

        // TODO: Implement.

        return fields;
    }

    private List<ParameterInfo> parseParameters(List<BrygParser.InDeclarationContext> contexts) {
        List<ParameterInfo> parameters = new ArrayList<>();
        for (BrygParser.InDeclarationContext context : contexts) {
            String name = IdUtil.idToString(context.id());
            Type type = new TypeInterpreter(environment.getClassResolver()).interpretType(context.type());

            // TODO: Change qualifier from opt to nullable.
            boolean isNullable = context.qualifier.getType() == BrygLexer.OPT;
            parameters.add(new ParameterInfo(type, name, Mutability.immutable,
                    isNullable ? Nullness.nullable : Nullness.notnull, null)); // TODO: Handle default values.
        }
        return parameters;
    }

}
