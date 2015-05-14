package io.collap.bryg.internal.type;

import io.collap.bryg.*;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeInterpreter {

    private static final Map<String, Type> typeNameToPrimitiveType = new HashMap<>();

    static {
        typeNameToPrimitiveType.put("void", Types.fromClass(Void.TYPE));
        typeNameToPrimitiveType.put("boolean", Types.fromClass(Boolean.TYPE));
        typeNameToPrimitiveType.put("char", Types.fromClass(Character.TYPE));
        typeNameToPrimitiveType.put("byte", Types.fromClass(Byte.TYPE));
        typeNameToPrimitiveType.put("short", Types.fromClass(Short.TYPE));
        typeNameToPrimitiveType.put("int", Types.fromClass(Integer.TYPE));
        typeNameToPrimitiveType.put("long", Types.fromClass(Long.TYPE));
        typeNameToPrimitiveType.put("float", Types.fromClass(Float.TYPE));
        typeNameToPrimitiveType.put("double", Types.fromClass(Double.TYPE));
    }

    /**
     * @return The primitive type associated with the type name or null if no association has been found.
     */
    private static @Nullable Type getPrimitiveTypeFromName(String typeName) {
        return typeNameToPrimitiveType.get(typeName);
    }

    private StandardEnvironment environment;

    public TypeInterpreter(StandardEnvironment environment) {
        this.environment = environment;
    }

    public Type interpretType(BrygParser.TypeContext ctx) {
        String typeName = IdUtil.idToString(ctx.id());

        // Closure are handled in a special way.
        if (typeName.equals("Closure")) {
            return interpretClosureType(ctx);
        }

        // Possible primitive type.
        if (ctx.children.size() == 1) { // Only Id is present!
            @Nullable Type type = getPrimitiveTypeFromName(typeName);
            if (type != null) {
                return type;
            }
        }

        // Possible class type.
        try {
            List<BrygParser.TypeContext> genericTypeContexts = ctx.type();
            boolean hasGenerics = !genericTypeContexts.isEmpty();

            // Don't fetch a cached type if the type needs generics.
            Type type = Types.fromClass(environment.getClassResolver().getResolvedClass(typeName),
                    hasGenerics);

            if (hasGenerics) {
                type.setGenericTypes(interpretTypes(genericTypeContexts));
            }

            return type;
        } catch (ClassNotFoundException e) {
            throw new BrygJitException("Could not interpret type '" + ctx.getText() + "'.", ctx.getStart().getLine(), e);
        }
    }

    private Type interpretClosureType(BrygParser.TypeContext ctx) {
        // TODO: Parameter Names? Nullable? Default values?
        // Also, since the following interface type is a globally used type, we need to construct a new ClosureType
        // that implements the interface to support the features mentioned in the TODO above. This would cause problems
        // with interoperability between different variables with ClosureTypes, because even when the interfaces match,
        // we would need to cast one value to the other type for an assignment.
        return environment.getOrCreateClosureInterface(interpretTypes(ctx.type()));
    }

    public List<Type> interpretTypes(List<BrygParser.TypeContext> typeContexts) {
        List<Type> result = new ArrayList<>();
        for (BrygParser.TypeContext genericTypeCtx : typeContexts) {
            Type genericType = interpretType(genericTypeCtx);
            result.add(genericType);
        }
        return result;
    }

}
