package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithLongReturn
implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        runHelp();
        assertFalse(Thread.holdsLock(this));
    }

    public synchronized long runHelp() {
        assertTrue(Thread.holdsLock(this));
        return -711;
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return new MonitorWithContext[] {
                new MonitorWithContext(this,
                                       getClass().getName() + ".runHelp()",
                                       "this",
                                       Thread.currentThread()),
        };
    }
}
