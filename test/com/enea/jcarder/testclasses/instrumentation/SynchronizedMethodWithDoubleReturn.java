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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedMethodWithDoubleReturn
implements SynchronizationTestIfc {

    public void go() {
        assertFalse(Thread.holdsLock(this));
        runHelp();
        assertFalse(Thread.holdsLock(this));
    }

    public synchronized double runHelp() {
        assertTrue(Thread.holdsLock(this));
        return -711;
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return new MonitorWithContext[] {
                new MonitorWithContext(this,
                                       getClass().getName() + ".runHelp()",
                                       "this",
                                       Thread.currentThread()),
        };
    }
}
