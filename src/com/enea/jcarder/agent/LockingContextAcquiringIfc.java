package com.enea.jcarder.agent;

import java.io.IOException;

import com.enea.jcarder.common.LockingContext;

public interface LockingContextAcquiringIfc {

    /*
     * Acquire an unique id for the provided LockingContext. The implementation
     * is recommended, but not required, to try to return the same id for
     * LockingContexts that are equal according to the
     * LockingContext.equals(Object other) method.
     */
    int acquireLockingContextId(LockingContext c) throws IOException;
}
