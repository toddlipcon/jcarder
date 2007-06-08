package com.enea.jcarder.common.contexts;

import java.io.IOException;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

public interface ContextWriterIfc {

    int writeLock(Lock lock) throws IOException;

    int writeContext(LockingContext context) throws IOException;
}
