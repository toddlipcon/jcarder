package com.enea.jcarder.agent.instrument;

import net.jcip.annotations.NotThreadSafe;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


@NotThreadSafe
class SimulateMethodSyncMethodAdapter extends MethodAdapter {
    private final String mClassName;
    private final boolean mIsStatic;
    private final Label mTryLabel = new Label();
    private final Label mFinallyLabel = new Label();

    SimulateMethodSyncMethodAdapter(final MethodVisitor visitor,
                                    final String className,
                                    final boolean isStatic) {
        super(visitor);
        mClassName = className;
        mIsStatic = isStatic;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        // This MethodAdapter will only be applied to synchronized methods,
        // and constructors is not allowed to be declared synchronized,
        // therefor we can add instructions at the begining of the method
        // and no not have to find the place after the initial constructor
        // byte codes:
        //     ALOAD 0: this
        //     INVOKESPECIAL Object.<init>() : void
        putMonitorObjectReferenceOnStack();
        mv.visitInsn(Opcodes.MONITORENTER);
        mv.visitLabel(mTryLabel);
    }

    /**
     * This method is called just after the last code in the method.
     */
    @Override
    public void visitMaxs(int arg0, int arg1) {
        // This finally block is needed in order to exit the monitor
        // even when the method is exited by throwing an exception.
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

    @Override
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
            InstrumentationUtilities.pushClassReferenceToStack(mv, mClassName);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
    }
}
