package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithMultipleFloatReturns
implements SynchronizationTestIfc {
    public void go() {
        assertFalse(Thread.holdsLock(this));
        assertEquals(-1.0F, runHelp(-5));
        assertFalse(Thread.holdsLock(this));

        assertFalse(Thread.holdsLock(this));
        assertEquals(1.0F, runHelp(5));
        assertFalse(Thread.holdsLock(this));
    }

    public synchronized float runHelp(float foo) {
        assertTrue(Thread.holdsLock(this));
        if (foo < 0) {
            return -1.0F;
        } else {
            return 1.0F;
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return new MonitorWithContext[] {
                new MonitorWithContext(this,
                                       getClass().getName() + ".runHelp()",
                                       "this",
                                       Thread.currentThread()),
                new MonitorWithContext(this,
                                       getClass().getName() + ".runHelp()",
                                       "this",
                                       Thread.currentThread())
        };
    }
}
