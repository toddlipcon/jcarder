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

public final class RepeatOlderSynchronization
implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();

    public void go() {
        synchronized (mSync0) {
            assertTrue(Thread.holdsLock(mSync0));
            foo();
        }
    }

    // This method is extracted from go() in order to give an example
    // when it is not obvious what to do, since the method foo() might
    // also be invoked from somewhere else.
    public void foo() {
        synchronized (mSync1) {
            assertTrue(Thread.holdsLock(mSync1));
            synchronized (mSync0) {
                assertTrue(Thread.holdsLock(mSync0));
            }
        }
    }


    public LockEvent[] getExpectedLockEvents() {
        final Lock lockSync0 = new Lock(mSync0);
        final Lock lockSync1 = new Lock(mSync1);
        final String threadName = Thread.currentThread().getName();
        final String method = getClass().getName() + ".go()";
        final LockingContext contextSync0 =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync0",
                               method);
        final LockingContext contextSync0Foo =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync0",
                               getClass().getName() + ".foo()");
        final LockingContext contextSync1Foo =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync1",
                               getClass().getName() + ".foo()");
        return new LockEvent[] {
            new LockEvent(true, lockSync0, contextSync0),
            new LockEvent(true, lockSync1, contextSync1Foo),
            new LockEvent(true, lockSync0, contextSync0Foo),

            new LockEvent(false, lockSync0, contextSync0Foo),
            new LockEvent(false, lockSync1, contextSync1Foo),
            new LockEvent(false, lockSync0, contextSync0)
        };
    }
}
