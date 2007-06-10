package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedStaticMethodWithException
implements SynchronizationTestIfc {

    public void go() {
        try {
            assertFalse(Thread.holdsLock(getClass()));
            help();
            fail("Missing RuntimeException");
        } catch (RuntimeException e) {
            assertFalse(Thread.holdsLock(getClass()));
        }
    }

    public static synchronized void help() {
        Class clazz = SynchronizedStaticMethodWithException.class;
        assertTrue(Thread.holdsLock(clazz));
        try {
            throw new RuntimeException("test");
        } catch (RuntimeException e) {
            assertTrue(Thread.holdsLock(clazz));
            throw e;
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(getClass(),
                                         getClass().getName() + ".help()",
                                         "class");
    }
}
