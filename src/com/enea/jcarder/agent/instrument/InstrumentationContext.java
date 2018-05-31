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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;


class InstrumentationContext {
    private String mClassName;
    private String mMethodName = "<unknown>";
    private String mSourceFile = "<unknown>";
    private int mLineNumber = -1;

    public InstrumentationContext(String className) {
        mClassName = className;
    }

    public void setSourceFile(String sourceFile) {
        mSourceFile = sourceFile;
    }

    public String getSourceFile() {
        return mSourceFile;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setMethodName(String name) {
        mMethodName = name;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public void setLineNumber(int l) {
        mLineNumber = l;
    }

    public int getLineNumber() {
        return mLineNumber;
    }

    public String getCallContextString() {
        return mClassName + "." + mMethodName + "() (" +
            mSourceFile + ":" + mLineNumber + ")";
    }

    public String convertFromJvmInternalNames(String s) {
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


    public MethodVisitor getLineNumberWatcherAdapter(MethodVisitor mv) {
        return new LineNumberMethodAdapter(mv);
    }

    private class LineNumberMethodAdapter extends MethodVisitor {
        public LineNumberMethodAdapter(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            mLineNumber = line;
            super.visitLineNumber(line, start);
        }

    }
}
