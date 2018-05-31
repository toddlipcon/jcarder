package com.enea.jcarder.testclasses.instrumentation;

import com.enea.jcarder.agent.LockTracer;
import com.enea.jcarder.agent.instrument.MonitorWithContext;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReentrantLockSynchronization implements SynchronizationTestIfc {

    private final ReentrantLock lock = new ReentrantLock();

    public void go() {
        assertFalse(lock.isHeldByCurrentThread());
        try {
            lock.lock();
            assertTrue(lock.isHeldByCurrentThread());
        } finally {
            lock.unlock();
        }
        assertFalse(lock.isHeldByCurrentThread());
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(LockTracer.getSyncObject(lock),
                                         getClass(), "go",
                                         getClass().getName() + ".lock",
                                         17);
    }
}