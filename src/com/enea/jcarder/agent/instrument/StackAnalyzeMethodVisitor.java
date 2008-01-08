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

import java.util.Stack;
import net.jcip.annotations.NotThreadSafe;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.enea.jcarder.util.logging.Logger;

/**
 * This class tries to keep track of what is currently on the operand stack. It
 * does not keep track of the actual values but from where the values
 * originates. A value may for example originate from a specific field member in
 * the class, a local variable, a return value from a specific method or
 * something else.
 *
 * The analysis is done during the instrumentation.
 */
@NotThreadSafe
class StackAnalyzeMethodVisitor implements MethodVisitor {
    private static final TextualDescription UNKOWN_VALUE =
        new TextualDescription("???");
    private final Logger mLogger;
    private final Stack<Object> mStack = new Stack<Object>();
    private final MethodVisitor mMethodVisitor;
    private final boolean mIsStatic;

    StackAnalyzeMethodVisitor(final Logger logger,
                              final MethodVisitor methodVisitor,
                              final boolean isStatic) {
        mLogger = logger;
        mMethodVisitor = methodVisitor;
        mIsStatic = isStatic;
    }

    private static class TextualDescription {
        private final String mDescription;

        TextualDescription(String description) {
            mDescription = description;
        }

        public String toString() {
            return mDescription;
        }
    }

    /**
     * @return A textual description of from where the current value of the
     *         stack originates. The string "???" is returned if the origin is
     *         unknown.
     */
    String peek() {
        if (mStack.isEmpty()) {
            return UNKOWN_VALUE.toString();
        } else {
            return mStack.peek().toString();
        }
    }

    private String pop() {
        return popObject().toString();
    }

    private Object popObject() {
        if (mStack.isEmpty()) {
            return UNKOWN_VALUE;
        } else {
            return mStack.pop();
        }
    }

    private void pushTextualDescription(String s) {
        mStack.push(new TextualDescription(s));
    }

    private void pushStringObject(String s) {
        mStack.push(s);
    }

    private void clear() {
        mLogger.finest("Invalidating stack");
        mStack.clear();
    }

    public void visitCode() {
        mMethodVisitor.visitCode();
        clear();
    }

    public void visitEnd() {
        mMethodVisitor.visitEnd();
        clear();
    }

    public void visitFieldInsn(int opCode,
                               String owner,
                               String name,
                               String desc) {
        mMethodVisitor.visitFieldInsn(opCode, owner, name, desc);
        switch (opCode) {
        case Opcodes.GETFIELD:
            pop();
            pushTextualDescription(owner + "." + name);
            break;
        case Opcodes.GETSTATIC:
            pushTextualDescription(owner + "." + name);
            break;
        default:
            clear();
        }
    }

    public void visitIincInsn(int arg0, int arg1) {
        mMethodVisitor.visitIincInsn(arg0, arg1);
        clear();
    }

    public void visitInsn(int opCode) {
        mMethodVisitor.visitInsn(opCode);
        switch (opCode) {
        case Opcodes.DUP:
            pushTextualDescription(peek());
            break;
        default:
            clear();
        }
    }

    public void visitIntInsn(int opCode, int arg1) {
        mMethodVisitor.visitIntInsn(opCode, arg1);
        clear();
    }

    public void visitJumpInsn(int opCode, Label arg1) {
        mMethodVisitor.visitJumpInsn(opCode, arg1);
        clear();
    }

    public void visitLabel(Label arg0) {
        mMethodVisitor.visitLabel(arg0);
        // We have to invalidate the stack since we don't know how we arrived
        // at this label. We might have jumped to this place from anywhere.
        clear();
    }

    public void visitLdcInsn(Object cst) {
        mMethodVisitor.visitLdcInsn(cst);
        if (cst instanceof Type) {
            Type t = (Type) cst;
            pushTextualDescription(t.getClassName() + ".class");
        } else if (cst instanceof String) {
            pushStringObject((String) cst);
        } else {
            clear();
        }
    }

    public void visitLineNumber(int arg0, Label arg1) {
        mMethodVisitor.visitLineNumber(arg0, arg1);
    }

    public void visitLocalVariable(String name,
                                   String desc,
                                   String signature,
                                   Label start,
                                   Label end,
                                   int index) {
        mMethodVisitor.visitLocalVariable(name,
                                          desc,
                                          signature,
                                          start,
                                          end,
                                          index);
    }

    public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
        mMethodVisitor.visitLookupSwitchInsn(arg0, arg1, arg2);
        clear();
    }

    // TODO refactor this method
    public void visitMethodInsn(int opCode,
                                String owner,
                                String name,
                                String desc) {
        mMethodVisitor.visitMethodInsn(opCode, owner, name, desc);
        switch (opCode) {
        case Opcodes.INVOKEVIRTUAL:
            // pass through to next case.
        case Opcodes.INVOKESPECIAL:
            // pass through to next case.
        case Opcodes.INVOKESTATIC:
            if ("forName".equals(name)
                && "java/lang/Class".equals(owner)
                && "(Ljava/lang/String;)Ljava/lang/Class;".equals(desc)) {
                Object stackObject = popObject();
                if (stackObject instanceof String) {
                    String classDescription = ((String) stackObject) + ".class";
                    pushTextualDescription(classDescription);
                    break;
                }
            }
            // pass through to next case.
        case Opcodes.INVOKEINTERFACE:
            clear();
            if (isNonVoidMethod(name, desc)) {
                pushTextualDescription(owner + "." + name + "()");
            }
            break;
        default:
            clear();
        }
    }

    private static boolean isNonVoidMethod(String name, String desc) {
        return Type.getReturnType(desc) != Type.VOID_TYPE
               || name.equals("<init>");
    }

    public void visitMultiANewArrayInsn(String arg0, int arg1) {
        mMethodVisitor.visitMultiANewArrayInsn(arg0, arg1);
        clear();
    }

    public AnnotationVisitor visitParameterAnnotation(int arg0,
                                                      String arg1,
                                                      boolean arg2) {
        return mMethodVisitor.visitParameterAnnotation(arg0, arg1, arg2);
    }

    public void visitTableSwitchInsn(int arg0,
                                     int arg1,
                                     Label arg2,
                                     Label[] arg3) {
        mMethodVisitor.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
        clear();
    }

    public void visitTryCatchBlock(Label arg0,
                                   Label arg1,
                                   Label arg2,
                                   String arg3) {
        mMethodVisitor.visitTryCatchBlock(arg0, arg1, arg2, arg3);
        clear();
    }

    public void visitTypeInsn(int opCode, String desc) {
        mMethodVisitor.visitTypeInsn(opCode, desc);
    }

    public void visitVarInsn(int opCode, int index) {
        mMethodVisitor.visitVarInsn(opCode, index);
        switch (opCode) {
        case Opcodes.ALOAD:
            if (index == 0 && !mIsStatic) {
                pushTextualDescription("this");
            } else {
                /*
                 * TODO Translate the index to a local variable name. To be able
                 * to do that we probably have to analyze the class in two steps
                 * since the visit method visitLocalVariable is not called until
                 * after all calls to visitVarInsn.
                 */
                pushTextualDescription("<localVariable" + index + ">");
            }
            break;
        case Opcodes.ASTORE:
            pop();
            break;
        default:
            clear();
        }
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        return mMethodVisitor.visitAnnotation(arg0, arg1);
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return mMethodVisitor.visitAnnotationDefault();
    }

    public void visitAttribute(Attribute arg0) {
        mMethodVisitor.visitAttribute(arg0);
    }

    public void visitMaxs(int arg0, int arg1) {
        mMethodVisitor.visitMaxs(arg0, arg1);
    }
}
