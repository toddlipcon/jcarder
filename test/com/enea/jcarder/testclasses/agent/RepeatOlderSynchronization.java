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
            // It can be argued if the mSync1 -> mSync0 transition should be
            // reported or not. If foo() is only called from run() it
            // should be safe to ignore the transtion, but if foo() can
            // be called directly that transition is needed to be able to
            // find potential deadlocks.
            //
            // The transition is NOT reported in the current implementation
            // in order to avoid false warnings and we rely on that all
            // possible invocations of foo() is covered with the users test
            // scenarios.
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
        final LockingContext contextSync1 =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync1",
                               getClass().getName() + ".foo()");
        return new LockEvent[] {
            new LockEvent(lockSync1, contextSync1, lockSync0, contextSync0)
        };
    }
}
