package com.enea.jcarder.testclasses.agent;

import com.enea.jcarder.agent.LockEvent;

public interface SynchronizationTestIfc {
    void go() throws Exception;
    LockEvent[] getExpectedLockEvents();
}
