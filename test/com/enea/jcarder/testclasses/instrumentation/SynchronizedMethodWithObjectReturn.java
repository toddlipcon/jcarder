package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithObjectReturn
implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        runHelp();
        assertFalse(Thread.holdsLock(this));
    }

    public synchronized Object runHelp() {
        assertTrue(Thread.holdsLock(this));
        return "fsdfasdfdsa";
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
