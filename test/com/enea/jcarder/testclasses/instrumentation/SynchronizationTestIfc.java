package com.enea.jcarder.testclasses.instrumentation;

import com.enea.jcarder.agent.instrument.MonitorWithContext;


public interface SynchronizationTestIfc {
    void go() throws Exception;
    MonitorWithContext[] getExpectedMonitorEnterings();
}
