package io.collap.bryg.test;

import io.collap.bryg.compiler.Configuration;
import io.collap.bryg.compiler.StandardCompiler;
import io.collap.bryg.compiler.library.BasicLibrary;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.resolver.PrefixFilter;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.loader.FileSourceLoader;
import io.collap.bryg.loader.SourceLoader;
import io.collap.bryg.loader.TemplateClassLoader;
import io.collap.bryg.model.GlobalVariableModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Domain {

    private static Environment environment = null;

    private static void setupEnvironment () {
        PrintStream originalOut = System.out;
        try {
            File log = new File ("build/test/setup.log");
            log.getParentFile ().mkdirs ();
            log.createNewFile ();
            PrintStream fileOutput = new PrintStream (new FileOutputStream (log));
            System.setOut (fileOutput);
        } catch (IOException e) {
            throw new RuntimeException ("Could not redirect environment setup log", e);
        }

        Configuration configuration = new Configuration ();
        configuration.setPrintParseTree (true);
        configuration.setPrintAst (true);
        configuration.setPrintBytecode (true);

        SourceLoader sourceLoader = new FileSourceLoader (new File ("src/test/resources"));
        ClassResolver classResolver = new ClassResolver ();
        classResolver.addFilter (new PrefixFilter ("io.collap.bryg.test"));
        classResolver.resolveClassNames ();

        Library library = new BasicLibrary ();

        GlobalVariableModel commonModel = new GlobalVariableModel ();
        commonModel.declareVariable ("globalString", String.class, "This is the value of a global variable.");

        io.collap.bryg.compiler.Compiler compiler = new StandardCompiler (configuration, library, classResolver, commonModel);

        environment = new StandardEnvironment (new TemplateClassLoader (compiler, sourceLoader), commonModel);

        System.setOut (originalOut);
    }

    public static synchronized Environment getEnvironment () {
        if (environment == null) setupEnvironment ();
        return environment;
    }

}
