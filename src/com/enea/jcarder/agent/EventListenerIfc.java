package com.enea.jcarder.agent;

import com.enea.jcarder.common.LockingContext;

// TODO better name on this interface, it is not always a deadlock
public interface EventListenerIfc {

    void beforeMonitorEnter(final Object newMonitor,
                            final LockingContext newContext) throws Exception;

}
