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

package com.enea.jcarder.agent;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.enea.jcarder.agent.instrument.ClassTransformer;
import com.enea.jcarder.agent.instrument.InstrumentConfig;
import com.enea.jcarder.agent.instrument.TransformClassLoader;
import com.enea.jcarder.common.contexts.ContextMemory;
import com.enea.jcarder.common.events.LockEventListenerIfc;
import com.enea.jcarder.testclasses.agent.ComparableAlternativeSynchronizationRoutes;
import com.enea.jcarder.testclasses.agent.RepeatMostRecentlySynchronization;
import com.enea.jcarder.testclasses.agent.RepeatOlderSynchronization;
import com.enea.jcarder.testclasses.agent.SingleSynchronization;
import com.enea.jcarder.testclasses.agent.SynchronizationTestIfc;
import com.enea.jcarder.testclasses.agent.TwoThreadSynchronization;
import com.enea.jcarder.util.logging.Logger;

/**
 * The purpose of this junit class is to test the MonitorEventListener class.
 *
 * But it serves also as an integration test with the MonitorEventListener and
 * the com.enea.jcarder.agent.instrument package. One interesting aspect of the
 * integration is that MonitorEventListener/EnteredMonitor depends on that
 * the monitor events are sent BEFORE instead of AFTER the monitor is entered.
 */
public final class TestMonitorEventListener implements LockEventListenerIfc {
    private ContextMemory mContextMemory;
    private final TransformClassLoader mClassLoader;
    private final LinkedList<LockEvent> mEvents = new LinkedList<LockEvent>();

    public TestMonitorEventListener() {
        ClassTransformer transformer =
            new ClassTransformer(new Logger(null),
                                 new File("."),
                                 new InstrumentConfig());
        mClassLoader = new TransformClassLoader(transformer);
    }

    @Before
    public void setUp() throws Exception {
        mContextMemory = new ContextMemory();
        StaticEventListener.setListener(new EventListener(new Logger(null),
                                                          this,
                                                          mContextMemory));
    }

    private void testClass(Class clazz) throws Exception {
        SynchronizationTestIfc test = transformAsSynchronizationTest(clazz);
        test.go();

//        String actual = Arrays.deepToString(mEvents.toArray());
//        System.out.println("  Actual: " + actual);
//        String expected = Arrays.deepToString(test.getExpectedLockEvents());
//        System.out.println("Expected: " + expected);

        assertEquals(test.getExpectedLockEvents(),
                     mEvents.toArray());
    }

    private SynchronizationTestIfc transformAsSynchronizationTest(Class clazz)
    throws Exception {
        Class c = mClassLoader.transform(clazz);
        return (SynchronizationTestIfc) c.newInstance();
    }

    public void onLockEvent(boolean isLock,
                            int lockId,
                            int lockingContextId,
                            long threadId)
    throws IOException {
        ContextMemory cm = mContextMemory;
        LockEvent event =
            new LockEvent(isLock,
                          cm.readLock(lockId),
                          cm.readContext(lockingContextId));
        mEvents.add(event);
    }

    @Test
    public void testRepeatMostRecentlySynchronization() throws Exception {
        testClass(RepeatMostRecentlySynchronization.class);
    }

    @Test
    public void testRepeatOlderSynchronization() throws Exception {
        testClass(RepeatOlderSynchronization.class);
    }

    @Test
    public void testComparableAlternativeSynchronizationRoutes()
    throws Exception {
        testClass(ComparableAlternativeSynchronizationRoutes.class);
    }

    @Test
    public void testSingleSynchronization() throws Exception {
        testClass(SingleSynchronization.class);
    }

    @Test
    public void testTwoThreadSynchronization() throws Exception {
        testClass(TwoThreadSynchronization.class);
    }
}
