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
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.enea.jcarder.agent.StaticEventListener;

import static com.enea.jcarder.agent.instrument.InstrumentationUtilities.getInternalName;

@NotThreadSafe
class MonitorEnterMethodAdapter extends MethodAdapter {
    private static final String CALLBACK_CLASS_NAME =
        getInternalName(StaticEventListener.class);
    private final String mClassAndMethodName;
    private final String mClassName;
    private StackAnalyzeMethodVisitor mStack;

    MonitorEnterMethodAdapter(final MethodVisitor visitor,
                          final String className,
                          final String methodName) {
        super(visitor);
        mClassAndMethodName = className + "." + methodName + "()";
        mClassName = className;
    }

    public void visitInsn(int inst) {
        if (inst == Opcodes.MONITORENTER) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(convertFromJvmInternalNames(mStack.peek()));
            mv.visitLdcInsn(mClassAndMethodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                               CALLBACK_CLASS_NAME,
                               "beforeMonitorEnter",
                   "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
        }
        super.visitInsn(inst);
    }

    private String convertFromJvmInternalNames(String s) {
        if (s == null) {
            assert false;
            return "null???";
        } else {
            final String name = s.replace('/', '.');
            if (name.equals(mClassName + ".class")) {
                return "class";
            } else {
                return name;
            }
        }
    }

    void setStackAnalyzer(StackAnalyzeMethodVisitor stack) {
        mStack = stack;
    }
}
