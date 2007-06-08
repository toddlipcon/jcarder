package com.enea.jcarder.agent;

import com.enea.jcarder.common.LockingContext;

public interface EventListenerIfc {

    void beforeMonitorEnter(Object monitor,
                            LockingContext context) throws Exception;

}
