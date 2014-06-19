package io.collap.bryg.compiler.type;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class TypeInterpreter {

    private StandardVisitor visitor;

    public TypeInterpreter (StandardVisitor visitor) {
        this.visitor = visitor;
    }

    @Nullable
    public Class<?> interpretType (BrygParser.TypeContext ctx) {
        String typeName = ctx.Id ().getText ();

        /* Possible primitive type. */
        if (ctx.children.size () == 1) { /* Only Id is present! */
            Class<?> type = Types.getPrimitiveTypeFromName (typeName);
            if (type != null) {
                return type;
            }
        }

        /* Possible class type. */
        try {
            return visitor.getClassResolver ().getResolvedClass (typeName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }

        return null;
    }

}
