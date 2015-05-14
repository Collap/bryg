package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.internal.compiler.TemplateCompiler;
import io.collap.bryg.internal.compiler.TemplateParser;
import io.collap.bryg.internal.compiler.UnitInterfaceCompiler;
import io.collap.bryg.module.Member;
import io.collap.bryg.module.Module;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This implementation can only handle templates without constructor parameters or
 * templates that are a subclass of StandardTemplate.
 */
public class StandardEnvironment implements Environment {

    private final Map<String, TemplateType> templateTypes = Collections.synchronizedMap(new HashMap<>());
    private final Map<List<Type>, ClosureInterfaceType> closureInterfaces = new HashMap<>();

    private Map<String, Module> modules = new HashMap<>();
    private Map<String, Module> globalNameToModuleMap = new HashMap<>();
    private List<SourceLoader> sourceLoaders = new ArrayList<>();
    private ClassResolver classResolver;
    private DebugConfiguration debugConfiguration = new DebugConfiguration(false, false, false, false);

    private StandardClassLoader standardClassLoader;

    public void initialize() {
        standardClassLoader = new StandardClassLoader(this);
    }

    public void addModule(Module module) {
        if (modules.containsKey(module.getName())) {
            throw new IllegalArgumentException("A module with the name " + module.getName() + " is already registered!");
        }

        // Add each member to the global member space when the module is globally visible.
        if (module.getVisibility() == Visibility.global) {
            Iterator<? extends Member<?>> iterator = module.getMemberIterator();
            while (iterator.hasNext()) {
                Member<?> member = iterator.next();
                registerGlobalMember(module, member);
            }
        }

        modules.put(module.getName(), module);
    }

    private void registerGlobalMember (Module module, Member<?> member) {
        String memberName = member.getName();
        if (globalNameToModuleMap.containsKey(memberName)) {
            throw new RuntimeException("A member with the name '" + memberName + "' was already registered by the " +
                    "module " + globalNameToModuleMap.get(memberName).getName() + ". Current module: " + module.getName());
        }

        globalNameToModuleMap.put(memberName, module);
    }

    public @Nullable Module getModule(String name) {
        return modules.get(name);
    }

    public @Nullable Member<?> getGlobalMember(String name) {
        @Nullable Module module = globalNameToModuleMap.get(name);
        if (module == null) {
            return null;
        }
        return module.getMember(name);
    }

    public void addSourceLoader(SourceLoader sourceLoader) {
        sourceLoaders.add(sourceLoader);
    }

    /**
     * Ensures that exactly one interface exists per different Closure&lt;...&gt; definition to
     * allow interoperability.
     */
    public synchronized ClosureInterfaceType getOrCreateClosureInterface(List<Type> parameterTypes) {
        @Nullable ClosureInterfaceType interfaceType = closureInterfaces.get(parameterTypes);
        if (interfaceType == null) {
            interfaceType = new ClosureInterfaceType("io.collap.bryg.closures.ClosureInterface" + closureInterfaces.size());

            // Directly set the generic types as a precaution, so that they are not accidentally specified.
            interfaceType.setGenericTypes(Collections.emptyList());

            // We need to construct parameters.
            List<ParameterInfo> parameters = new ArrayList<>(parameterTypes.size());
            int i = 0;
            for (Type parameterType : parameterTypes) {
                parameters.add(new ParameterInfo(
                        parameterType, "p" + i, Mutability.immutable, Nullness.notnull, null
                ));
                i += 1;
            }

            interfaceType.addFragment(
                    new FragmentInfo(interfaceType, UnitType.DEFAULT_FRAGMENT_NAME, true, parameters)
            );

            // And finally compile the interface so the JVM knows what's up.
            UnitInterfaceCompiler compiler = new UnitInterfaceCompiler(interfaceType);
            standardClassLoader.addCompiler(compiler);
            try {
                Class<? extends Closure> interfaceClass = (Class<? extends Closure>)
                        standardClassLoader.loadClass(interfaceType.getFullName());
                interfaceType.setInterfaceClass(interfaceClass);
            } catch (ClassNotFoundException e) {
                throw new CompilationException("Could not create closure interface with the following parameter types: " +
                        parameterTypes);
            }

            closureInterfaces.put(parameterTypes, interfaceType);
        }
        return interfaceType;
    }

    /**
     * This method is thread-safe. We use a combination of a synchronized map
     * and a synchronized parsing method to avoid making this method synchronized
     * as well.
     */
    public @Nullable TemplateType getTemplateType(String name) {
        @Nullable TemplateType templateType = templateTypes.get(name);

        if (templateType == null) {
            templateType = parseTemplate(name);
        }

        return templateType;
    }

    /**
     * This method is thread-safe. We use a combination of a synchronized map
     * and synchronized compilation/parsing methods to avoid making this method
     * synchronized as well.
     */
    public TemplateFactory getTemplateFactory(String name) {
        @Nullable TemplateType templateType = getTemplateType(name);

        if (templateType == null) {
            throw new CompilationException("The template type corresponding to the template name '" + name
                    + "' could not be loaded.");
        }

        if (!templateType.isCompiled()) {
            boolean success = compileTemplate(templateType);
            if (!success) {
                throw new CompilationException("There was an error during template compilation.");
            }
        }

        return new StandardTemplateFactory(this, templateType); // TODO: Cache the factory?
    }

    private synchronized @Nullable TemplateType parseTemplate(String name) {
        @Nullable TemplateType templateType = templateTypes.get(name);

        // A similar strategy like in compileTemplate is used here to make sure that templates
        // are not parsed twice.
        if (templateType == null) {
            System.out.println();
            System.out.println("Parsing template: " + name);

            @Nullable String source = null;
            for (SourceLoader sourceLoader : sourceLoaders) {
                source = sourceLoader.getTemplateSource(name);
                if (source != null) {
                    break;
                }
            }

            if (source == null) {
                throw new CompilationException("The source for the template '" + name + "' could not be found.");
            }

            TemplateParser parser = new TemplateParser(this, name, source);
            templateType = parser.parse();
            templateTypes.put(name, templateType);

            System.out.println();
        }

        return templateType;
    }

    /**
     * Also adds the template constructor to the cache.
     *
     * @return Whether the template was loaded correctly.
     */
    private synchronized boolean compileTemplate(TemplateType templateType) {
        /* There could be a case where getTemplate is called with the same name two or more times,
           which would result in the following scenario:
             1. The first call reaches compileTemplate and calls it. compileTemplate is then locked on this object.
             2. The second call reaches compileTemplate and calls it.
             3. The first call finishes.
             4. The second call can now enter compileTemplate and *compiles the template again*.
           The check ensures that the template is not compiled again. */
        if (!templateType.isCompiled()) {
            try {
                String className = templateType.getFullName();

                System.out.println();
                System.out.println("Compiling template: " + className);
                // long start = System.nanoTime ();

                TemplateCompiler compiler = new TemplateCompiler(this, templateType);
                standardClassLoader.addCompiler(compiler);

                Class<? extends Template> cl = (Class<? extends Template>) standardClassLoader.loadClass(className);
                templateType.setTemplateClass(cl);

                // Compile all referenced templates.
                templateType.getCompilationData().getReferencedTemplates().forEach(this::compileTemplate);

                // Remove references to temporary compilation data.
                templateType.clearCompilationData();

                // System.out.println ("Loading took " + ((System.nanoTime () - start) / 1.0e9) + "s.");
                System.out.println();
            } catch (ClassNotFoundException e) {
                throw new CompilationException("Template " + templateType.getFullName() + " could not be compiled.", e);
            }
        }

        return true;
    }

    public List<SourceLoader> getSourceLoaders() {
        return sourceLoaders;
    }

    public ClassResolver getClassResolver() {
        return classResolver;
    }

    public void setClassResolver(ClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    public DebugConfiguration getDebugConfiguration() {
        return debugConfiguration;
    }

    public void setDebugConfiguration(DebugConfiguration debugConfiguration) {
        this.debugConfiguration = debugConfiguration;
    }

    public StandardClassLoader getStandardClassLoader() {
        return standardClassLoader;
    }

}
