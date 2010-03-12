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
  private String mClassAndMethodName;
  private String mClassName;

  private static final String REENTRANTLOCK_INTERNAL_NAME =
    "java/util/concurrent/locks/ReentrantLock";
  private static final String LOCK_INTERNAL_NAME =
    "java/util/concurrent/locks/Lock";
  private static final String TRACING_REENTRANTLOCK_INTERNAL_NAME =
    "com/enea/jcarder/agent/instrument/TracingReentrantLock";


  LockClassSubstituterAdapter(final MethodVisitor visitor,
                              String methodName,
                              String className) {
    super(visitor);
    mClassAndMethodName = className + "." + methodName + "()";
    mClassName = className;
  }

  void setStackAnalyzer(StackAnalyzeMethodVisitor stack) {
    mStack = stack;
  }

  @Override
  public void visitMethodInsn(int opcode,
                              String owner, String name, String desc) {

    System.err.println("method. opcode: " + opcode + 
                       " owner: " + owner +
                       " name: " + name + 
                       " desc: " + desc);
    if ((opcode == Opcodes.INVOKEVIRTUAL ||
         opcode == Opcodes.INVOKEINTERFACE) && 
        (REENTRANTLOCK_INTERNAL_NAME.equals(owner) ||
         LOCK_INTERNAL_NAME.equals(owner))) {

      String traceCallSpec = null;

      if ("lock".equals(name) ||
          "unlock".equals(name) ||
          "lockInterruptibly".equals(name)) {

        traceCallSpec = "(Ljava/util/concurrent/locks/Lock;Ljava/lang/String;Ljava/lang/String;)V";
      } else if ("tryLock".equals(name)) {
        traceCallSpec = "(Ljava/util/concurrent/locks/Lock;Ljava/lang/String;Ljava/lang/String;)Z";
      }

      if (traceCallSpec != null) {
        mv.visitLdcInsn(convertFromJvmInternalNames(mStack.peek()));
        mv.visitLdcInsn(mClassAndMethodName);

        mv.visitMethodInsn(
          Opcodes.INVOKESTATIC,
          "com/enea/jcarder/agent/LockTracer",
          name, traceCallSpec);
        return;
      } else {
        System.err.println("Didn't know how to instrument call to " +
                           owner + "." + name + desc);
      }
    }
    mv.visitMethodInsn(opcode, owner, name, desc);
  }

  // TODO(tlipcon) make me util
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

}
