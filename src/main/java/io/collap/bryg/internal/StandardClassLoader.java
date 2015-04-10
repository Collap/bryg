package io.collap.bryg.internal;

import io.collap.bryg.internal.compiler.Compiler;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;

public final class StandardClassLoader extends ClassLoader {

    private final StandardEnvironment environment;
    private final LinkedList<Compiler<?>> compilers = new LinkedList<>();

    public StandardClassLoader(StandardEnvironment environment) {
        this(StandardClassLoader.class.getClassLoader(), environment);
    }

    public StandardClassLoader(ClassLoader parent, StandardEnvironment environment) {
        super(parent);
        this.environment = environment;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Find a compiler that matches the name of the class which loading is requested.
        @Nullable Compiler<?> compiler = null;
        synchronized (compilers) {
            Iterator<Compiler<?>> iterator = compilers.listIterator();
            while (iterator.hasNext()) {
                Compiler<?> nextCompiler = iterator.next();
                if (nextCompiler.getUnitType().getFullName().equals(name)) {
                    // Choose the compiler and remove it from the list.
                    compiler = nextCompiler;
                    iterator.remove();
                }
            }
        }

        // No compiler for the requested name has been found.
        if (compiler == null) {
            return super.findClass(name);
        }

        System.out.println("Class name: " + name);

        byte[] bytecode = compiler.compile();
        return defineClass(name, bytecode, 0, bytecode.length);
    }

    /**
     * Adds a compiler to the expected list. It will be removed once the compilation is done.
     */
    public void addCompiler(Compiler compiler) {
        synchronized (compilers) {
            compilers.addFirst(compiler);
        }
    }

}
