package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethod implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        help();
        assertFalse(Thread.holdsLock(this));
    }

    public synchronized void help() {
        assertTrue(Thread.holdsLock(this));
    }


    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(this,
                                         getClass().getName() + ".help()",
                                         "this");
    }

}
