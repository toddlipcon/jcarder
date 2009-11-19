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
import com.enea.jcarder.common.LockingContext;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public final class TwoThreadSynchronization extends Thread
implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();
    private final Object mSync2 = new Object();

    public void go() throws InterruptedException {
        assertFalse(Thread.holdsLock(mSync0));
        assertFalse(Thread.holdsLock(mSync1));
        synchronized (mSync0) {
            assertTrue(Thread.holdsLock(mSync0));
            synchronized (mSync1) {
                assertTrue(Thread.holdsLock(mSync1));
                start();
                Thread.yield();
                Thread.sleep(100);
            }
            assertFalse(Thread.holdsLock(mSync1));
            join();
        }
        assertFalse(Thread.holdsLock(mSync0));
        assertFalse(Thread.holdsLock(mSync1));
    }

    public void run() {
        assertFalse(Thread.holdsLock(mSync2));
        assertFalse(Thread.holdsLock(mSync1));
        assertFalse(Thread.holdsLock(mSync0));
        synchronized (mSync2) {
            assertTrue(Thread.holdsLock(mSync2));
            synchronized (mSync1) {
                assertTrue(Thread.holdsLock(mSync1));
            }
        }
        assertFalse(Thread.holdsLock(mSync2));
        assertFalse(Thread.holdsLock(mSync1));
    }

    public LockEvent[] getExpectedLockEvents() {
        final Lock lockSync0 = new Lock(mSync0);
        final Lock lockSync1 = new Lock(mSync1);
        final Lock lockSync2 = new Lock(mSync2);
        final String threadName = Thread.currentThread().getName();
        final String method = getClass().getName() + ".go()";
        LockingContext contextSync0 =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync0",
                               method);
        LockingContext contextSync1 =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync1",
                               method);
        LockingContext contextSync1Thread =
            new LockingContext(getName(),
                               getClass().getName() + ".mSync1",
                               getClass().getName() + ".run()");

        LockingContext contextSync2Thread =
            new LockingContext(getName(),
                               getClass().getName() + ".mSync2",
                               getClass().getName() + ".run()");
        return new LockEvent[] {
            new LockEvent(true, lockSync0, contextSync0),
            new LockEvent(true, lockSync1, contextSync1),
            new LockEvent(true, lockSync2, contextSync2Thread),
            new LockEvent(true, lockSync1, contextSync1Thread),
            new LockEvent(false, lockSync1, contextSync1),
            new LockEvent(false, lockSync1, contextSync1Thread),
            new LockEvent(false, lockSync2, contextSync2Thread),
            new LockEvent(false, lockSync0, contextSync0)
        };
    }
}
