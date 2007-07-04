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

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedNewObject implements SynchronizationTestIfc {
    public Object mDummy;

    public void go() {
        assertFalse(Thread.holdsLock(this));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized (new Object()) { }
        assertFalse(Thread.holdsLock(this));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(null,
                                         getClass().getName() + ".go()",
                                         "java.lang.Object.<init>()");
    }
}
