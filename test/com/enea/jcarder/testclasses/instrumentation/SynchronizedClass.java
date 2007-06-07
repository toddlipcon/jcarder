package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedClass implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(String.class));
        synchronized (String.class) {
            assertTrue(Thread.holdsLock(String.class));
        }
        assertFalse(Thread.holdsLock(String.class));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(String.class,
                                         getClass().getName() + ".go()",
                                         "java.lang.String.class");
    }
}
