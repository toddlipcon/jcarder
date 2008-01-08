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

public final class SynchronizedStaticMethod implements SynchronizationTestIfc {
        public void go() {
            assertFalse(Thread.holdsLock(getClass()));
            help();
            assertFalse(Thread.holdsLock(getClass()));
        }

        public static synchronized void help() {
            assertTrue(Thread.holdsLock(SynchronizedStaticMethod.class));
        }

        public MonitorWithContext[] getExpectedMonitorEnterings() {
            return MonitorWithContext.create(getClass(),
                                             getClass().getName() + ".help()",
                                             "class");
        }
}

