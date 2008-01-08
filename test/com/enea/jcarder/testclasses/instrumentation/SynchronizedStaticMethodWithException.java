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
import static org.junit.Assert.fail;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedStaticMethodWithException
implements SynchronizationTestIfc {

    public void go() {
        try {
            assertFalse(Thread.holdsLock(getClass()));
            help();
            fail("Missing RuntimeException");
        } catch (RuntimeException e) {
            assertFalse(Thread.holdsLock(getClass()));
        }
    }

    public static synchronized void help() {
        Class clazz = SynchronizedStaticMethodWithException.class;
        assertTrue(Thread.holdsLock(clazz));
        try {
            throw new RuntimeException("test");
        } catch (RuntimeException e) {
            assertTrue(Thread.holdsLock(clazz));
            throw e;
        }
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(getClass(),
                                         getClass().getName() + ".help()",
                                         "class");
    }
}
