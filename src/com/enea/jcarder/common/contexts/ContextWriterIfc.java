package com.enea.jcarder.common.contexts;

import java.io.IOException;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

public interface ContextWriterIfc {

    int writeLock(Lock lock) throws IOException;

    int writeLockingContext(LockingContext context) throws IOException;
}
