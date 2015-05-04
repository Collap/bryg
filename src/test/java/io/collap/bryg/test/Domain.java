package io.collap.bryg.test;

import io.collap.bryg.*;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.module.GenericModule;
import io.collap.bryg.module.HtmlModule;
import io.collap.bryg.module.MemberVariable;
import io.collap.bryg.test.object.TestObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Domain {

    private static Environment environment = null;

    private static void setupEnvironment() {
        PrintStream originalOut = System.out;
        try {
            File log = new File("build/test/setup.log");
            log.getParentFile().mkdirs();
            log.createNewFile();
            PrintStream fileOutput = new PrintStream(new FileOutputStream(log));
            System.setOut(fileOutput);
        } catch (IOException e) {
            throw new RuntimeException("Could not redirect environment setup log", e);
        }

        EnvironmentBuilder builder = new StandardEnvironmentBuilder();
        builder.setDebugConfiguration(new DebugConfiguration(
                true,   // print tokens
                true,   // print parse tree
                true,   // print bytecode
                true    // print AST
        ));
        builder.registerSourceLoader(new FileSourceLoader(new File("src/test/resources")));
        ClassResolver classResolver = new ClassResolver();
        classResolver.getRootPackageTree().addPackage("io.collap.bryg.test.object");
        classResolver.resolve();
        builder.setClassResolver(classResolver);

        // Register HTML module.
        builder.registerModule(new HtmlModule(Visibility.global));

        // TODO: Create wrapper for member variable, to reduce redundancy and prevent .internal imports.
        GenericModule globalVariableModule = new GenericModule("test", Visibility.global);
        globalVariableModule.setMember("globalString", new MemberVariable<>(
                globalVariableModule, Types.fromClass(String.class), "globalString",
                "This is the value of a global variable", Nullness.notnull)
        );
        globalVariableModule.setMember("testObject", new MemberVariable<>(
                globalVariableModule, Types.fromClass(TestObject.class), "testObject",
                new TestObject(), Nullness.notnull
        ));
        builder.registerModule(globalVariableModule);

        environment = builder.build();
        System.setOut(originalOut);
    }

    public static synchronized Environment getEnvironment() {
        if (environment == null) setupEnvironment();
        return environment;
    }

}
