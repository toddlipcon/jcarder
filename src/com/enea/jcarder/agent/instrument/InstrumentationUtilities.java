/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.agent.instrument;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class InstrumentationUtilities {

    private InstrumentationUtilities() { }

    public static void pushClassReferenceToStack(MethodVisitor mv,
                                                 String className) {
        /*
         * It is not possible to use:
         *
         *     mv.visitLdcInsn(RuleType.getType(mClassName));
         *
         * for class versions before 49.0 (introduced with java 1.5). Therefore
         * we use Class.forName instead.
         *
         * TODO It might be possible to do this more efficiently by caching the
         * result from Class.forName. But note that adding a new field (where
         * the cached class object can be stored) is only possible if the class
         * has not already been loaded by the JVM.
         */
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
