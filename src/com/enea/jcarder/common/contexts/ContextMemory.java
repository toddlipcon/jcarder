package com.enea.jcarder.common.contexts;

import java.io.IOException;
import java.util.LinkedList;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;

@NotThreadSafe
public final class ContextMemory
implements ContextWriterIfc, ContextReaderIfc {

    private final LinkedList<Lock> mLocks = new LinkedList<Lock>();
    private final LinkedList<LockingContext> mLockingContexts
    = new LinkedList<LockingContext>();

    public int writeLock(Lock lock) throws IOException {
        mLocks.addLast(lock);
        return mLocks.size() - 1;
    }

    public int writeLockingContext(LockingContext context) throws IOException {
        mLockingContexts.addLast(context);
        return mLockingContexts.size() - 1;
    }

    public Lock readLock(int id) {
        return mLocks.get(id);
    }

    public LockingContext readLockingContext(int id) {
        return mLockingContexts.get(id);
    }
}
