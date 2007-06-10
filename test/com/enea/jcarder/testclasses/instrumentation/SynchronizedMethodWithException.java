package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.*;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithException
implements SynchronizationTestIfc {

    public void go() {
        try {
            assertFalse(Thread.holdsLock(this));
            runHelp();
            fail("Missing RuntimeException");
        } catch (RuntimeException e) {
            assertFalse(Thread.holdsLock(this));
            assertEquals("test2", e.getMessage());
        }
    }

    public synchronized void runHelp() {
        assertTrue(Thread.holdsLock(this));
        try {
            throw new RuntimeException("test");
        } catch (RuntimeException e) {
            assertTrue(Thread.holdsLock(this));
            throw new RuntimeException("test2");
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(this,
                                         getClass().getName() + ".runHelp()",
                                         "this");
    }

}
