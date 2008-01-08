/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

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
