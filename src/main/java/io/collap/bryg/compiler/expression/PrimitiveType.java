package io.collap.bryg.compiler.expression;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public enum PrimitiveType implements Type {

    _void,
    _boolean,
    _char,
    _byte,
    _short,
    _int,
    _long,
    _float,
    _double;

    private static final BiMap<Class<?>, PrimitiveType> classToPrimitiveTypeMap = HashBiMap.create ();
    private static final BiMap<PrimitiveType, Class<?>> primitiveTypeToClassMap = classToPrimitiveTypeMap.inverse ();


    static {
        classToPrimitiveTypeMap.put (Void.TYPE, _void);
        classToPrimitiveTypeMap.put (Boolean.TYPE, _boolean);
        classToPrimitiveTypeMap.put (Character.TYPE, _char);
        classToPrimitiveTypeMap.put (Byte.TYPE, _byte);
        classToPrimitiveTypeMap.put (Short.TYPE, _short);
        classToPrimitiveTypeMap.put (Integer.TYPE, _int);
        classToPrimitiveTypeMap.put (Long.TYPE, _long);
        classToPrimitiveTypeMap.put (Float.TYPE, _float);
        classToPrimitiveTypeMap.put (Double.TYPE, _double);
    }

    @Override
    public String toDescriptorFormat () {
        switch (this) {
            case _void:     return "V";
            case _boolean:  return "Z";
            case _char:     return "C";
            case _byte:     return "B";
            case _short:    return "S";
            case _int:      return "I";
            case _long:     return "J";
            case _float:    return "F";
            case _double:   return "D";
        }
        return null;
    }

    @Override
    public Class<?> getActualType () {
        return primitiveTypeToClassMap.get (this);
    }

    /**
     * @return The PrimitiveType associated with the type or null if no association has been found.
     */
    public static PrimitiveType fromJavaTypeName (String typeName) {
        try {
            return valueOf ("_" + typeName);
        }catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static PrimitiveType fromJavaClass (Class<?> cl) {
        return classToPrimitiveTypeMap.get (cl);
    }

}
