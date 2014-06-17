package io.collap.bryg.compiler.expression;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class TypeInterpreter {

    private RenderVisitor visitor;

    public TypeInterpreter (RenderVisitor visitor) {
        this.visitor = visitor;
    }

    @Nullable
    public Type interpretType (BrygParser.TypeContext ctx) {
        String typeName = ctx.Id ().getText ();

        /* Possible primitive type. */
        if (ctx.children.size () == 1) { /* Only Id is present! */
            PrimitiveType primitiveType = PrimitiveType.fromJavaTypeName (typeName);
            if (primitiveType != null) {
                return primitiveType;
            }
        }

        /* Possible class type. */
        try {
            Class<?> typeClass = visitor.getClassResolver ().getResolvedClass (typeName);
            return new ClassType (typeClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }

        return null;
    }

}
