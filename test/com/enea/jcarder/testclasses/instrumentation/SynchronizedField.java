package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedField implements SynchronizationTestIfc {
    public Object mDummy;
    private final Object mSync = new Object();

    public void go() {
        assertFalse(Thread.holdsLock(mSync));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized (mSync) {
            assertTrue(Thread.holdsLock(mSync));
        }
        assertFalse(Thread.holdsLock(mSync));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(mSync,
                                         getClass().getName() + ".go()",
                                         getClass().getName() + ".mSync");
    }
}
