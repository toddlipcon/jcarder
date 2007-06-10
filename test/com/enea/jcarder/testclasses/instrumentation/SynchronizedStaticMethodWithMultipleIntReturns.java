package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedStaticMethodWithMultipleIntReturns
implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        assertEquals(-1, help(-5));
        assertFalse(Thread.holdsLock(this));

        assertFalse(Thread.holdsLock(this));
        assertEquals(1, help(5));
        assertFalse(Thread.holdsLock(this));
    }

    public static synchronized int help(int foo) {
        Class clazz = SynchronizedStaticMethodWithMultipleIntReturns.class;
        assertTrue(Thread.holdsLock(clazz));
        if (foo < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return new MonitorWithContext[] {
                new MonitorWithContext(getClass(),
                                       getClass().getName() + ".help()",
                                       "class",
                                       Thread.currentThread()),
                new MonitorWithContext(getClass(),
                                       getClass().getName() + ".help()",
                                       "class",
                                       Thread.currentThread())
        };
    }
}
