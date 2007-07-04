/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.*;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithException
implements SynchronizationTestIfc {

    public void go() {
        try {
            assertFalse(Thread.holdsLock(this));
            runHelp();
            fail("Missing RuntimeException");
        } catch (RuntimeException e) {
            assertFalse(Thread.holdsLock(this));
            assertEquals("test2", e.getMessage());
        }
    }

    public synchronized void runHelp() {
        assertTrue(Thread.holdsLock(this));
        try {
            throw new RuntimeException("test");
        } catch (RuntimeException e) {
            assertTrue(Thread.holdsLock(this));
            throw new RuntimeException("test2");
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(this,
                                         getClass().getName() + ".runHelp()",
                                         "this");
    }

}
