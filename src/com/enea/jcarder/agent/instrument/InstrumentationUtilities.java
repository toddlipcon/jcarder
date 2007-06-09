package com.enea.jcarder.agent.instrument;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class InstrumentationUtilities {

    private InstrumentationUtilities() { }

    public static void pushClassReferenceToStack(MethodVisitor mv,
                                                 String className) {
        // It is not possible to use:
        //
        //    mv.visitLdcInsn(RuleType.getType(mClassName));
        //
        // for class versions before 49.0 (introduced with java 1.5).
        // Therefore we use Class.forName instead.
        //
        // TODO It might be possible to do this more efficiently
        //      by caching the result from Class.forName.
        //      But note that adding a new field (where the cached class
        //      object can be stored) is only possible if the class has
        //      not already been loaded by the JVM.
        //
        mv.visitLdcInsn(className);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                           "java/lang/Class",
                           "forName",
                           "(Ljava/lang/String;)Ljava/lang/Class;");
    }

    public static String getInternalName(Class c) {
        return c.getName().replace('.', '/');
    }
}
