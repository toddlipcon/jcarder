package com.enea.jcarder.agent.instrument;

import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import net.jcip.annotations.NotThreadSafe;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.enea.jcarder.util.Logger;

/**
 * Each instance of this class is responsible for instrumenting a class. It
 * uses the MonitorMethodAdapter for instrumenting each method in the class.
 */
@NotThreadSafe
class ClassAdapter extends org.objectweb.asm.ClassAdapter {
    private final String mClassName;
    private final boolean mRedefiningAlreadyLoadedClass;
    private final Logger mLogger = Logger.getLogger(this);

    ClassAdapter(ClassVisitor visitor,
                         String className,
                         boolean redefiningAlreadyLoadedClass) {
            super(visitor);
            mClassName = className;
            mRedefiningAlreadyLoadedClass = redefiningAlreadyLoadedClass;
            mLogger.fine("Dead lock instrumenting the class: " + mClassName);
    }

    @Override
    public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
        super.visit(arg0, arg1, arg2, arg3, arg4, arg5);
        super.visitAttribute(new InstrumentedAttribute("DeadLock"));
    }

    @Override
    public MethodVisitor visitMethod(final int arg,
                                     final String methodName,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        final boolean isSynchronized = (arg & ACC_SYNCHRONIZED) != 0;
        final boolean isNative = (arg & ACC_NATIVE) != 0;
        final boolean isStatic = (arg & ACC_STATIC) != 0;
        if (isNative) {
            mLogger.finer("Can't instrument native method:"
                          + mClassName + "." + methodName);
            return super.visitMethod(arg,
                                     methodName,
                                     descriptor,
                                     signature,
                                     exceptions);
        } else {
            // We want to be able to get an event before a
            // synchronizated method is entered and BEFORE it
            // has taken the lock in order to notice deadlocks
            // before they acctually happened. Therefore we replace
            // the synchronized declaration of the method with
            // explicit monitorEnter and monitorExit bytecodes
            // in the begining of the method and at each possible
            // leaving (by normal return och by exception) of the
            // method.
            //
            // Another alternative could have been to rename the
            // existing synchronize declared method and add another
            // wrapper method (with the previous name) which make
            // sure that the wanted events are sent and that the
            // renamed method is invoked. But it is not possible
            // to add a new method when redefining an already loaded
            // class. Another dissadvantage with that alternative is
            // that the stacktraces would look strange and IDEs
            // might be confused.
            final int manipulatedArg;
            if (isSynchronized) {
                if (!mRedefiningAlreadyLoadedClass) {
                    manipulatedArg = arg & ~ACC_SYNCHRONIZED;
                } else {
                    mLogger.warning("Can't attach deadlock probe to "
                                   + "the synchronized method "
                                   + mClassName + "." + methodName);
                    manipulatedArg = arg;
                }
            } else {
                manipulatedArg = arg;
            }
            final MethodVisitor mv = super.visitMethod(manipulatedArg,
                                                       methodName,
                                                       descriptor,
                                                       signature,
                                                       exceptions);
            final DeadLockMethodAdapter dlma =
                new DeadLockMethodAdapter(mv,
                                          mClassName,
                                          methodName);
            final StackAnalyzeMethodVisitor stackAnalyzer =
                new StackAnalyzeMethodVisitor(dlma, isStatic);
            dlma.setStackAnalyzer(stackAnalyzer);
            if (arg != manipulatedArg) {
                return new SimulateMethodSyncMethodAdapter(stackAnalyzer,
                                                           mClassName,
                                                           isStatic);
            } else {
                return stackAnalyzer;
            }
        }
    }
}
