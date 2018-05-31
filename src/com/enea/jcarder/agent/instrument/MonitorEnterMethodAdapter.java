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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.enea.jcarder.agent.StaticEventListener;

import static com.enea.jcarder.agent.instrument.InstrumentationUtilities.getInternalName;

@NotThreadSafe
class MonitorEnterMethodAdapter extends MethodVisitor {
    private static final String CALLBACK_CLASS_NAME =
        getInternalName(StaticEventListener.class);
    private final InstrumentationContext mContext;
    private StackAnalyzeMethodVisitor mStack;


    MonitorEnterMethodAdapter(final MethodVisitor visitor,
                              final InstrumentationContext context) {
        super(Opcodes.ASM5, visitor);
        mContext = context;
    }

    @Override
    public void visitInsn(int inst) {
        if (inst == Opcodes.MONITORENTER) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(mContext.convertFromJvmInternalNames(mStack.peek()));
            mv.visitLdcInsn(mContext.getCallContextString());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                               CALLBACK_CLASS_NAME,
                               "beforeMonitorEnter",
                   "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
        } else if (inst == Opcodes.MONITOREXIT) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(mContext.convertFromJvmInternalNames(mStack.peek()));
            mv.visitLdcInsn(mContext.getCallContextString());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                               CALLBACK_CLASS_NAME,
                               "beforeMonitorExit",
                   "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V");
        }

        super.visitInsn(inst);
    }

    void setStackAnalyzer(StackAnalyzeMethodVisitor stack) {
        mStack = stack;
    }
}
