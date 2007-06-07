package com.enea.jcarder.common.contexts;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

public interface ContextReaderIfc {
    LockingContext readLockingContext(int id);
    Lock readLock(int id);
}
