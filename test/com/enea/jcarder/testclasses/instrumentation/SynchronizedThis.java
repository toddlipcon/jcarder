package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedThis implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        synchronized (this) {
            assertTrue(Thread.holdsLock(this));
        }
        assertFalse(Thread.holdsLock(this));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(this,
                                         getClass().getName() + ".go()",
                                         "this");
    }
}
