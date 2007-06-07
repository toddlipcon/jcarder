package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedNull implements SynchronizationTestIfc {
    public Object mSync = null;
    public Object mSync2 = new Object();

    public void go() {
        try {
            synchronized (mSync) { }
        } catch (NullPointerException e) {
            // Expecting NullPointerException.
            assertFalse(Thread.holdsLock(mSync2));
            synchronized (mSync2) {
                assertTrue(Thread.holdsLock(mSync2));
            }
            assertFalse(Thread.holdsLock(mSync2));
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(mSync2,
                                         getClass().getName() + ".go()",
                                         getClass().getName() + ".mSync2");
    }
}
