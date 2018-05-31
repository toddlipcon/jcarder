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
import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RepeatMostRecentlySynchronization
implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();

    public void go() {
        synchronized (mSync0) {
            assertFalse(Thread.holdsLock(mSync1));
            synchronized (mSync1) {
                assertTrue(Thread.holdsLock(mSync1));
                // The following synchronization used to be ignored, though
                // now it actually records it since it can determine redundancy
                // at analysis time.
                synchronized (mSync1) {
                    assertTrue(Thread.holdsLock(mSync1));
                }
            }
        }
    }

    public LockEvent[] getExpectedLockEvents() {
        final Lock lockSync0 = new Lock(mSync0);
        final Lock lockSync1 = new Lock(mSync1);
        return new LockEvent[] {
            LockEvent.create(LockEventType.MONITOR_ENTER, lockSync0, getClass(), "go", "mSync0", 32),
            LockEvent.create(LockEventType.MONITOR_ENTER, lockSync1, getClass(), "go", "mSync1", 34),
            LockEvent.create(LockEventType.MONITOR_ENTER, lockSync1, getClass(), "go", "mSync1", 39),
            LockEvent.create(LockEventType.MONITOR_EXIT, lockSync1, getClass(), "go", "mSync1", 41),
            LockEvent.create(LockEventType.MONITOR_EXIT, lockSync1, getClass(), "go", "mSync1", 42),
            LockEvent.create(LockEventType.MONITOR_EXIT, lockSync0, getClass(), "go", "mSync0", 43)
        };
    }

}
