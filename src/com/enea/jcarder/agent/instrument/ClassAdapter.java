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

import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import net.jcip.annotations.NotThreadSafe;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.enea.jcarder.util.logging.Logger;

/**
 * Each instance of this class is responsible for instrumenting a class. It uses
 * the MonitorMethodAdapter for instrumenting each method in the class.
 */
@NotThreadSafe
class ClassAdapter extends org.objectweb.asm.ClassAdapter {
    private final String mClassName;
    private final Logger mLogger;

    ClassAdapter(Logger logger, ClassVisitor visitor, String className) {
        super(visitor);
        mLogger = logger;
        mClassName = className;
        mLogger.fine("Instrumenting class " + mClassName);
    }

    public void visit(int arg0, int arg1, String arg2, String arg3, String arg4,
                      String[] arg5) {
        super.visit(arg0, arg1, arg2, arg3, arg4, arg5);
        super.visitAttribute(new InstrumentedAttribute("DeadLock"));
    }

    public MethodVisitor visitMethod(final int arg,
                                     final String methodName,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        final boolean isSynchronized = (arg & ACC_SYNCHRONIZED) != 0;
        final boolean isNative = (arg & ACC_NATIVE) != 0;
        final boolean isStatic = (arg & ACC_STATIC) != 0;
        final int manipulatedArg = arg & ~ACC_SYNCHRONIZED;
        if (isNative) {
            mLogger.finer("Can't instrument native method "
                          + mClassName + "." + methodName);
            return super.visitMethod(arg,
                                     methodName,
                                     descriptor,
                                     signature,
                                     exceptions);
        } else {
            final MethodVisitor mv = super.visitMethod(manipulatedArg,
                                                       methodName,
                                                       descriptor,
                                                       signature,
                                                       exceptions);
            final MonitorEnterMethodAdapter dlma =
                new MonitorEnterMethodAdapter(mv, mClassName, methodName);
            final StackAnalyzeMethodVisitor stackAnalyzer =
                new StackAnalyzeMethodVisitor(mLogger, dlma, isStatic);
            dlma.setStackAnalyzer(stackAnalyzer);
            if (isSynchronized) {
                /*
                 * We want to be able to get an event before a synchronized
                 * method is entered and BEFORE it has taken the lock in order
                 * to notice deadlocks before they actually happen. Therefore we
                 * replace the synchronized declaration of the method with
                 * explicit monitorEnter and monitorExit bytecodes in the
                 * beginning of the method and at each possible exit (by normal
                 * return and by exception) of the method.
                 */
                return new SimulateMethodSyncMethodAdapter(stackAnalyzer,
                                                           mClassName,
                                                           isStatic);
            } else {
                return stackAnalyzer;
            }
        }
    }
}
