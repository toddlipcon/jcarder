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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassWriter;

public final class InstrumentedAttribute extends Attribute {
    private static final String PREFIX = "com.enea.jcarder.instrumented";

    public InstrumentedAttribute() {
        super(PREFIX);
    }

    public InstrumentedAttribute(String attributeType) {
        super(PREFIX + "." + attributeType);
    }

    public static boolean matchAttribute(Attribute a) {
        return a.type.startsWith(PREFIX);
    }

    protected ByteVector write(ClassWriter arg0,
                               byte[] arg1,
                               int arg2,
                               int arg3,
                               int arg4) {
        return new ByteVector();
    }
}
