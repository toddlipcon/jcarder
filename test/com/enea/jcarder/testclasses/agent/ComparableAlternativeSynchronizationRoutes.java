package com.enea.jcarder.testclasses.agent;

import com.enea.jcarder.agent.LockEvent;
import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ComparableAlternativeSynchronizationRoutes
implements SynchronizationTestIfc {
    private final Object mSync0 = new Object();
    private final Object mSync1 = new Object();
    private final Object mSync2 = new Object();

    public void go() {
        assertFalse(Thread.holdsLock(mSync0));
        synchronized (mSync0) {
            assertTrue(Thread.holdsLock(mSync0));
            assertFalse(Thread.holdsLock(mSync1));
            synchronized (mSync1) {
                assertTrue(Thread.holdsLock(mSync1));
                assertFalse(Thread.holdsLock(mSync2));
                synchronized (mSync2) {
                    assertTrue(Thread.holdsLock(mSync2));
                }
            }
            assertFalse(Thread.holdsLock(mSync1));
            assertFalse(Thread.holdsLock(mSync2));

            // The transition from mSync0 -> mSync2 is already
            // "represented" as mSync0 -> mSync1 -> mSync2 which is enough
            // for the cycle-detection algorithm, but the following transition
            // is also included since the user might want to see a "complete"
            // graph with all transitions.
            //
            // If the number of events needs to be reduced it might be possible
            // to make this behaviour configurable for the user.
            synchronized (mSync2) {
                assertTrue(Thread.holdsLock(mSync2));
            }
        }
        assertFalse(Thread.holdsLock(mSync0));
        assertFalse(Thread.holdsLock(mSync1));
        assertFalse(Thread.holdsLock(mSync2));
    }

    /*
    If the behaviour is made configurable to the user as described above
    it might be interesting to also optimze the following scenarios:

    public void foo1() {
      synchronized(a) {
        synchronized(b) {
           synchronized(c) {
             ...
        }
        // The following duplicated a -> b transition was implicitly
        // lost/ignored with the old implementation when the onLockEvent was
        // sent AFTER instead of BEFORE the monitor was entered.
        synchronized(b) {
           ...
        }
      }
    }

    // A more generic example than foo1().
    public void foo2() {
      synchronized(a) {
        synchronized(b) {
           ...
        }
      }
      // A possibility to minimize repeated groups of transitions could be
      // to not remove monitors from the stack of aquired monitors when it
      // is not needed and instead keep a pointer the the current last
      // taken monitor.
      synchronized(a) {
        synchronized(b) {
           ...
        }
      }
    }
    */

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
        LockingContext contextSync2 =
            new LockingContext(threadName,
                               getClass().getName() + ".mSync2",
                               method);

        return new LockEvent[] {
            new LockEvent(lockSync1, contextSync1, lockSync0, contextSync0),
            new LockEvent(lockSync2, contextSync2, lockSync1, contextSync1),
            new LockEvent(lockSync2, contextSync2, lockSync0, contextSync0),
        };
    }
}
