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

package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedFromStaticMethod
implements SynchronizationTestIfc {
    public Object mDummy;
    private static final Object SYNC = new Object();

    public static Object getSync() {
        return SYNC;
    }

    public void go() {
        assertFalse(Thread.holdsLock(SYNC));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized (getSync()) {
            assertTrue(Thread.holdsLock(SYNC));
        }
        assertFalse(Thread.holdsLock(SYNC));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(SYNC,
                                         getClass().getName() + ".go()",
                                         getClass().getName() + ".getSync()");
    }

}
