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
class SimulateMethodSyncMethodAdapter extends MethodAdapter {
    private final InstrumentationContext mContext;
    private final boolean mIsStatic;
    private final Label mTryLabel = new Label();
    private final Label mFinallyLabel = new Label();

    SimulateMethodSyncMethodAdapter(final MethodVisitor visitor,
                                    final InstrumentationContext context,
                                    final boolean isStatic) {
        super(visitor);
        mContext = context;
        mIsStatic = isStatic;
    }

    public void visitCode() {
        super.visitCode();
        /*
         * This MethodAdapter will only be applied to synchronized methods, and
         * constructors are not allowed to be declared synchronized. Therefore
         * we can add instructions at the beginning of the method and do not
         * have to find the place after the initial constructor byte codes:
         *
         *     ALOAD 0 : this
         *     INVOKESPECIAL Object.<init>() : void
         *
         */
        putMonitorObjectReferenceOnStack();
        mv.visitInsn(Opcodes.MONITORENTER);
        mv.visitLabel(mTryLabel);
    }

    /**
     * This method is called just after the last code in the method.
     */
    public void visitMaxs(int arg0, int arg1) {
        /*
         * This finally block is needed in order to exit the monitor even when
         * the method exits by throwing an exception.
         */
        mv.visitLabel(mFinallyLabel);
        putMonitorObjectReferenceOnStack();
        mv.visitInsn(Opcodes.MONITOREXIT);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitTryCatchBlock(mTryLabel,
                              mFinallyLabel,
                              mFinallyLabel,
                              null);
        super.visitMaxs(arg0, arg1);
    }

    public void visitInsn(int inst) {
        switch (inst) {
        case Opcodes.IRETURN:
        case Opcodes.LRETURN:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
        case Opcodes.ARETURN:
        case Opcodes.RETURN:
            putMonitorObjectReferenceOnStack();
            mv.visitInsn(Opcodes.MONITOREXIT);
            break;
        default:
            // Do nothing.
        }
        super.visitInsn(inst);
    }

    private void putMonitorObjectReferenceOnStack() {
        if (mIsStatic) {
            InstrumentationUtilities.pushClassReferenceToStack(mv, mContext.getClassName());
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
    }
}
