package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedInBlock implements SynchronizationTestIfc {
    private final Object mSync = new Object();

    {
        assertFalse(Thread.holdsLock(mSync));
        synchronized (mSync) {
            assertTrue(Thread.holdsLock(mSync));
        }
        assertFalse(Thread.holdsLock(mSync));
    }

    public void go() {

    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(mSync,
                                         getClass().getName() + ".<init>()",
                                         getClass().getName() + ".mSync");
    }
}
