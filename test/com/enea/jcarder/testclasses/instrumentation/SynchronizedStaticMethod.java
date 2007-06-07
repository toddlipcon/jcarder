package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedStaticMethod implements SynchronizationTestIfc {
        public void go() {
            assertFalse(Thread.holdsLock(getClass()));
            help();
            assertFalse(Thread.holdsLock(getClass()));
        }

        public static synchronized void help() {
            assertTrue(Thread.holdsLock(SynchronizedStaticMethod.class));
        }

        public MonitorWithContext[] getExpectedMonitorEnterings() {
            return MonitorWithContext.create(getClass(),
                                             getClass().getName() + ".help()",
                                             "class");
        }
}

