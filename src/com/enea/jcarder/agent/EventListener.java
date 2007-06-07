package com.enea.jcarder.agent;

import static com.enea.jcarder.common.contexts.ContextFileReader.EVENT_LOG_DB_FILE;
import static com.enea.jcarder.common.contexts.ContextFileReader.RANDOM_ACCESS_STORE_DB_FILE;

import java.io.IOException;
import java.util.Iterator;

import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextFileWriter;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.common.events.EventFileWriter;
import com.enea.jcarder.common.events.LockEventListenerIfc;
import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.Logger;

@ThreadSafe
/**
 * TODO I'm thinking about updating the DeadLock analyze classes to be able
 *      to use it with either the AsyncActionSerializer or the
 *      SyncActionSerializer because that would make it possible to enable
 *      the same analyzes at once. The performance may be affected by such a
 *      change so it would be good to have a performance test suite before.
 *
 *      It is possible that the AsyncActionSerializer will perform better
 *      than a SyncActionSerializer on a multi CPU-machine but the
 *      SyncActionSerializer would probably be faster on a singel CPU-machine
 *      with a single CPU-core.
 *
 *      If only the DeadLock analyze is runned and is not instrumenting the
 *      standard library, the SyncActionSerializer is probably still safest
 *      to use since the AsyncActionSerializer has some problems. Therefore
 *      any new features added for the DeadLock analyze, that require
 *      instrumentation of the standard library should probably be optional
 *      for the user.
 *
 *      @see com.enea.jcarder.agent.serializer.AsyncActionSerializer
 *      @see com.enea.jcarder.agent.serializer.SyncActionSerializer
 */
public final class EventListener implements EventListenerIfc {
    private final ThreadLocalEnteredMonitors mEnteredMonitors;
    private final LockEventListenerIfc mLockEventListener;
    private final LockIdAcquiringIfc mLockIdGenerator;
    private final LockingContextAcquiringIfc mContextCache;
    private final Logger mLogger;
    private final Counter mNumberOfEnteredMonitors;

    public static EventListener create() throws IOException {
        final EventFileWriter lockEventFileWriter
        = new EventFileWriter(EVENT_LOG_DB_FILE);

        final ContextWriterIfc ras
        = new ContextFileWriter(RANDOM_ACCESS_STORE_DB_FILE);

        final LockIdAcquiringIfc lockIdGenerator = new LockIdGenerator(ras);

        final LockingContextAcquiringIfc lockingContextIdCache
        = new LockingContextIdCache(ras);

        return new EventListener(lockEventFileWriter,
                                         lockIdGenerator,
                                         lockingContextIdCache);
    }

    public EventListener(LockEventListenerIfc lockEventListener,
                                 LockIdAcquiringIfc lockIdGenerator,
                                 LockingContextAcquiringIfc contextCache) {
        mLockEventListener = lockEventListener;
        mLockIdGenerator = lockIdGenerator;
        mContextCache = contextCache;
        mEnteredMonitors = new ThreadLocalEnteredMonitors();
        mLogger = Logger.getLogger("com.enea.jcarder");
        mNumberOfEnteredMonitors = new Counter("Entered Monitors",
                                               mLogger,
                                               100000);
    }

    public synchronized void beforeMonitorEnter(final Object newMonitor,
                                                final LockingContext newContext)
    throws Exception {
        mLogger.finest("DeadLockEventListener.beforeMonitorEnter");
        mNumberOfEnteredMonitors.increment();
        Iterator<EnteredMonitor> iter = mEnteredMonitors.getIterator();
        while (iter.hasNext()) {
            final EnteredMonitor knownEnteredMonitor = iter.next();
            final Object monitor = knownEnteredMonitor.getMonitorIfStillHeld();
            if (monitor == null) {
                iter.remove();
            } else if (monitor == newMonitor) {
                return; // Monitor already entered.
            }
        }
        onNewMonitorEnter(newMonitor, newContext);
    }

    private void onNewMonitorEnter(final Object newMonitor,
                                   final LockingContext newContext)
    throws Exception {
        int newLockId = mLockIdGenerator.acquireLockId(newMonitor);
        int newContextId = mContextCache.acquireLockingContextId(newContext);
        final EnteredMonitor source = mEnteredMonitors.getFirst();
        if (source != null) {
            Thread performingThread = Thread.currentThread();
            mLockEventListener.onLockEvent(newLockId,
                                           newContextId,
                                           source.getLockId(),
                                           source.getLockingContextId(),
                                           performingThread.getId());
        }
        mEnteredMonitors.addFirst(new EnteredMonitor(newMonitor,
                                                     newLockId,
                                                     newContextId));
    }
}
