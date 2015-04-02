package io.collap.bryg.test;

import io.collap.bryg.internal.compiler.Configuration;
import io.collap.bryg.library.BasicLibrary;
import io.collap.bryg.library.Library;
import io.collap.bryg.ClassResolver;
import io.collap.bryg.PrefixFilter;
import io.collap.bryg.Environment;
import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.FileSourceLoader;
import io.collap.bryg.SourceLoader;
import io.collap.bryg.GlobalVariableModel;
import io.collap.bryg.test.object.TestObject;

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
        classResolver.addFilter (new PrefixFilter ("io.collap.bryg.test.object"));
        classResolver.resolveClassNames ();

        Library library = new BasicLibrary ();

        GlobalVariableModel commonModel = new GlobalVariableModel ();
        commonModel.declareVariable ("globalString", String.class, "This is the value of a global variable.");
        commonModel.declareVariable ("testObject", TestObject.class, new TestObject ());

        environment = new StandardEnvironment (configuration, library, classResolver, sourceLoader, commonModel);

        System.setOut (originalOut);
    }

    public static synchronized Environment getEnvironment () {
        if (environment == null) setupEnvironment ();
        return environment;
    }

}
