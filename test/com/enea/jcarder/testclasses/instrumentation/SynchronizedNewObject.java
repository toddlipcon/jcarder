package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedNewObject implements SynchronizationTestIfc {
    public Object mDummy;

    public void go() {
        assertFalse(Thread.holdsLock(this));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized (new Object()) { }
        assertFalse(Thread.holdsLock(this));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(null,
                                         getClass().getName() + ".go()",
                                         "java.lang.Object.<init>()");
    }
}
