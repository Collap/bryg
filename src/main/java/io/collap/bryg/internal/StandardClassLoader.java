package io.collap.bryg.internal;

import io.collap.bryg.Unit;
import io.collap.bryg.internal.compiler.Compiler;

import java.util.Stack;

public final class StandardClassLoader extends ClassLoader {

    /**
     * This is the prefix of any unit class.
     */
    public static final String unitNamePrefix = "bryg.";

    private StandardEnvironment environment;
    private Stack<Compiler<?>> compilerStack;

    public StandardClassLoader(StandardEnvironment environment) {
        this (StandardClassLoader.class.getClassLoader(), environment);
    }

    public StandardClassLoader(ClassLoader parent, StandardEnvironment environment) {
        super(parent);
        this.environment = environment;
        this.compilerStack = new Stack<>();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.startsWith(unitNamePrefix)) {
            /* Try to load with default class loader. */
            System.out.println("TemplateClassLoader was requested to load '" + name + "'," +
                    " which does not have the mandatory prefix (" + unitNamePrefix + ")." +
                    "Attempting to use system class loader.");
            return Class.forName(name);
        }

        if (compilerStack.empty()) {
            throw new IllegalStateException("The compiler stack is empty, ");
        }

        // Only peek, since cleanup is performed manually. This also handles the case
        // where the compiler might only be used to get the name of the compiled unit,
        // in which case the compiler must not yet be popped.
        Compiler compiler = compilerStack.peek();

        String expectedName = compiler.getUnitType().getFullName();
        if (!name.equals(expectedName)) {
            // Try to load class from cache.
            // TODO: Probably obsolete now that there is only one class loader per environment.
            Class<? extends Unit> cl = environment.getClassCache ().getClass (name);
            if (cl == null) {
                throw new ClassNotFoundException ("The expected unit class '" + name + "' could not fetched from the cache.");
            }
            return cl;
        }

        System.out.println("Class name: " + name);

        byte[] bytecode = compiler.compile();
        return defineClass(name, bytecode, 0, bytecode.length);
    }

    /**
     * Pushes a compiler to the compiler stack. This marks it as the first compiler to be invoked.
     */
    public void pushCompiler(Compiler compiler) {
        compilerStack.push(compiler);
    }

    /**
     * Pops the top compiler from the stack. This method must be invoked for cleanup purposes.
     */
    public void popCompiler() {
        compilerStack.pop();
    }

    public static String getPrefixedName (String prefixlessName) {
        return unitNamePrefix + prefixlessName;
    }

    public static String getPrefixlessName (String name) {
        return name.substring (unitNamePrefix.length ());
    }

}
