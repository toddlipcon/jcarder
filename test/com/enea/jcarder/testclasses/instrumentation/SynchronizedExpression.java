package com.enea.jcarder.testclasses.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.enea.jcarder.agent.instrument.MonitorWithContext;

public final class SynchronizedExpression implements SynchronizationTestIfc {
    public boolean b = true;
    private final Object mSync = new Object();
    private final Object mOtherObject = new Object();
    public Object mDummy;

    public void go() {
        assertFalse(Thread.holdsLock(mSync));
        mDummy = new Object(); // Try to confuse StackAnalyzeMethodVisitor
        synchronized ((b ? mSync : mOtherObject)) {
            assertTrue(Thread.holdsLock(mSync));
        }
        assertFalse(Thread.holdsLock(mSync));
    }

    public MonitorWithContext[] getExpectedMonitorEnterings() {
        return MonitorWithContext.create(mSync,
                                         getClass().getName() + ".go()",
                                         "???");
    }
}
