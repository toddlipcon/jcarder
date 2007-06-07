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
