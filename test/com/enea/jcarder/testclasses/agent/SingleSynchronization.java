package com.enea.jcarder.testclasses.agent;

import com.enea.jcarder.agent.LockEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SingleSynchronization implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();

    public void go() {
        assertFalse(Thread.holdsLock(mSync0));
        // Synchronization on a single lock at a time can not cause any dead
        // lock and does not need to be reported.
        synchronized (mSync0) {
            assertTrue(Thread.holdsLock(mSync0));
        }
        assertFalse(Thread.holdsLock(mSync0));
        synchronized (mSync1) {
            assertTrue(Thread.holdsLock(mSync1));
        }
        assertFalse(Thread.holdsLock(mSync1));
    }

    public LockEvent[] getExpectedLockEvents() {
        return new LockEvent[0];
    }
}
