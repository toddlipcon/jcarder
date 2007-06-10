package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedFromStaticMethod
implements SynchronizationTestIfc {
    public Object mDummy;
    private static final Object SYNC = new Object();

    public static Object getSync() {
        return SYNC;
    }

    public void go() {
        assertFalse(Thread.holdsLock(SYNC));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized (getSync()) {
            assertTrue(Thread.holdsLock(SYNC));
        }
        assertFalse(Thread.holdsLock(SYNC));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(SYNC,
                                         getClass().getName() + ".go()",
                                         getClass().getName() + ".getSync()");
    }

}
