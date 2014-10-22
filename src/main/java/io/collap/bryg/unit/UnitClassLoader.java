package io.collap.bryg.unit;

import io.collap.bryg.Unit;
import io.collap.bryg.compiler.Compiler;
import io.collap.bryg.environment.Environment;

public abstract class UnitClassLoader extends ClassLoader {

    /**
     * This is the prefix of any unit class.
     */
    public static final String unitNamePrefix = "bryg.";

    protected Environment environment;
    protected io.collap.bryg.compiler.Compiler compiler;

    public UnitClassLoader (Environment environment, io.collap.bryg.compiler.Compiler compiler) {
        this (UnitClassLoader.class.getClassLoader (), environment, compiler);
    }

    public UnitClassLoader (ClassLoader parent, Environment environment, Compiler compiler) {
        super (parent);
        this.compiler = compiler;
        this.environment = environment;
    }

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException {
        if (!name.startsWith (unitNamePrefix)) {
            /* Try to load with default class loader. */
            System.out.println ("TemplateClassLoader was requested to load '" + name + "'," +
                    " which does not have the mandatory prefix (" + unitNamePrefix + ")." +
                    "Attempting to use system class loader.");
            return Class.forName (name);
        }

        String expectedName = compiler.getUnitType ().getFullName ();
        if (!name.equals (expectedName)) {
            /* Try to load class from cache. */
            Class<? extends Unit> cl = environment.getClassCache ().getClass (name);
            if (cl == null) {
                throw new ClassNotFoundException ("The expected unit class '" + name + "' could not fetched from the cache.");
            }
            return cl;
        }

        System.out.println ("Class name: " + name);

        byte[] bytecode = compiler.compile ();
        return defineClass (name, bytecode, 0, bytecode.length);
    }

    public static String getPrefixedName (String prefixlessName) {
        return unitNamePrefix + prefixlessName;
    }

    public static String getPrefixlessName (String name) {
        return name.substring (unitNamePrefix.length ());
    }

}
