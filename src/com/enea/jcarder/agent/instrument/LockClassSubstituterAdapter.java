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
import net.jcip.annotations.NotThreadSafe;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This Method Adapter simulates a synchronized declaration on a method by
 * adding a MonitorEnter and MonitorExits.
 */
@NotThreadSafe
class LockClassSubstituterAdapter extends MethodAdapter {
    private StackAnalyzeMethodVisitor mStack;

    private static final String REENTRANTLOCK_INTERNAL_NAME =
        "java/util/concurrent/locks/ReentrantLock";
    private static final String TRACING_REENTRANTLOCK_INTERNAL_NAME =
        "com/enea/jcarder/agent/instrument/TracingReentrantLock";

    LockClassSubstituterAdapter(final MethodVisitor visitor) {
        super(visitor);
    }

    void setStackAnalyzer(StackAnalyzeMethodVisitor stack) {
        mStack = stack;
    }

    @Override
    public void visitTypeInsn(int opcode,
                              String type) {
        if (opcode == Opcodes.NEW &&
            REENTRANTLOCK_INTERNAL_NAME.equals(type)) {

            mv.visitTypeInsn(opcode,
                             TRACING_REENTRANTLOCK_INTERNAL_NAME);
        } else {
            mv.visitTypeInsn(opcode, type);
        }
    }

	@Override
    public void visitMethodInsn(int opcode,
                                String owner, String name, String desc) {
        /*
          System.err.println("method. opcode: " + opcode + 
                           " owner: " + owner +
                           " name: " + name + 
                           " desc: " + desc);
        */
        if (opcode == Opcodes.INVOKESPECIAL &&
            REENTRANTLOCK_INTERNAL_NAME.equals(owner) &&
            "<init>".equals(name) &&
            "()V".equals(desc)) {

          // TODO add context info
            mv.visitLdcInsn("hello");
            mv.visitMethodInsn(
                opcode,
                "com/enea/jcarder/agent/instrument/TracingReentrantLock",
                "<init>",
                "(Ljava/lang/String;)V");
            System.err.println("Instruemented a ReentrantLock");
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }
}
