package io.collap.bryg.compiler.type;

import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeInterpreter {

    private static final Map<String, Type> typeNameToPrimitiveType = new HashMap<> ();

    static {
        typeNameToPrimitiveType.put ("void", new Type (Void.TYPE));
        typeNameToPrimitiveType.put ("boolean", new Type (Boolean.TYPE));
        typeNameToPrimitiveType.put ("char", new Type (Character.TYPE));
        typeNameToPrimitiveType.put ("byte", new Type (Byte.TYPE));
        typeNameToPrimitiveType.put ("short", new Type (Short.TYPE));
        typeNameToPrimitiveType.put ("int", new Type (Integer.TYPE));
        typeNameToPrimitiveType.put ("long", new Type (Long.TYPE));
        typeNameToPrimitiveType.put ("float", new Type (Float.TYPE));
        typeNameToPrimitiveType.put ("double", new Type (Double.TYPE));
    }

    /**
     * @return The primitive type associated with the type name or null if no association has been found.
     */
    private static Type getPrimitiveTypeFromName (String typeName) {
        return typeNameToPrimitiveType.get (typeName);
    }

    private StandardVisitor visitor;

    public TypeInterpreter (StandardVisitor visitor) {
        this.visitor = visitor;
    }

    @Nullable
    public Type interpretType (BrygParser.TypeContext ctx) {
        String typeName = IdHelper.idToString (ctx.id ());

        /* Possible primitive type. */
        if (ctx.children.size () == 1) { /* Only Id is present! */
            Type type = getPrimitiveTypeFromName (typeName);
            if (type != null) {
                return type;
            }
        }

        /* Possible class type. */
        try {
            Type type = new Type (visitor.getClassResolver ().getResolvedClass (typeName));

            /* Resolve generics. */
            List<BrygParser.TypeContext> genericTypeContexts = ctx.type ();
            List<Type> genericTypes = type.getGenericTypes ();
            for (BrygParser.TypeContext genericTypeCtx : genericTypeContexts) {
                Type genericType = interpretType (genericTypeCtx);
                genericTypes.add (genericType);
            }

            return type;
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }

        return null;
    }

}
