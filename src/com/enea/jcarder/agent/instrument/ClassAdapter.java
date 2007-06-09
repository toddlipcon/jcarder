package com.enea.jcarder.agent.instrument;

import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import net.jcip.annotations.NotThreadSafe;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.enea.jcarder.util.Logger;

/**
 * Each instance of this class is responsible for instrumenting a class. It uses
 * the MonitorMethodAdapter for instrumenting each method in the class.
 */
@NotThreadSafe
class ClassAdapter extends org.objectweb.asm.ClassAdapter {
    private final String mClassName;
    private final Logger mLogger = Logger.getLogger(this);

    ClassAdapter(ClassVisitor visitor, String className) {
        super(visitor);
        mClassName = className;
        mLogger.fine("Instrumenting the class: " + mClassName);
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
            mLogger.finer("Can't instrument native method:"
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
                new StackAnalyzeMethodVisitor(dlma, isStatic);
            dlma.setStackAnalyzer(stackAnalyzer);
            if (isSynchronized) {
                // We want to be able to get an event before a
                // synchronized method is entered and BEFORE it
                // has taken the lock in order to notice deadlocks
                // before they actually happen. Therefore we replace
                // the synchronized declaration of the method with
                // explicit monitorEnter and monitorExit bytecodes
                // in the beginning of the method and at each possible
                // exit (by normal return and by exception) of the
                // method.
                return new SimulateMethodSyncMethodAdapter(stackAnalyzer,
                                                           mClassName,
                                                           isStatic);
            } else {
                return stackAnalyzer;
            }
        }
    }
}
