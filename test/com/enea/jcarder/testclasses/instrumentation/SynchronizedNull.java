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

public final class SynchronizedNull implements SynchronizationTestIfc {
    public Object mSync = null;
    public Object mSync2 = new Object();

    public void go() {
        try {
            synchronized (mSync) { }
        } catch (NullPointerException e) {
            // Expecting NullPointerException.
            assertFalse(Thread.holdsLock(mSync2));
            synchronized (mSync2) {
                assertTrue(Thread.holdsLock(mSync2));
            }
            assertFalse(Thread.holdsLock(mSync2));
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(mSync2,
                                         getClass().getName() + ".go()",
                                         getClass().getName() + ".mSync2");
    }
}
