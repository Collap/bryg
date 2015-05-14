package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.ClassWriter;
import io.collap.bryg.internal.FragmentInfo;
import io.collap.bryg.internal.UnitType;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import java.util.Iterator;

import static bryg.org.objectweb.asm.Opcodes.*;

public class UnitInterfaceCompiler implements Compiler<UnitType> {

    private UnitType unitType;

    public UnitInterfaceCompiler(UnitType unitType) {
        this.unitType = unitType;
    }

    @Override
    public byte[] compile() {
        ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cv.visit(
                V1_7, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, TypeHelper.toInternalName(unitType.getFullName()), null,
                Types.fromClass(Object.class).getInternalName(), new String[]{});
        {
            Iterator<FragmentInfo> fragmentIterator = unitType.getFragmentIterator();
            while (fragmentIterator.hasNext()) {
                FragmentInfo fragment = fragmentIterator.next();
                cv.visitMethod(
                        ACC_PUBLIC + ACC_ABSTRACT,
                        fragment.getDirectName(), fragment.getDesc(),
                        null, null
                );
                // TODO: Delegator method?
            }
        }
        cv.visitEnd();
        return cv.toByteArray();
    }

    @Override
    public UnitType getUnitType() {
        return unitType;
    }

}
