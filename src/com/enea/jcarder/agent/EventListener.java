package com.enea.jcarder.agent;

import static com.enea.jcarder.common.contexts.ContextFileReader.EVENT_DB_FILE;
import static com.enea.jcarder.common.contexts.ContextFileReader.CONTEXTS_DB_FILE;

import java.io.IOException;
import java.util.Iterator;

import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextFileWriter;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.common.events.EventFileWriter;
import com.enea.jcarder.common.events.LockEventListenerIfc;
import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.logging.Logger;

@ThreadSafe
final class EventListener implements EventListenerIfc {
    private final ThreadLocalEnteredMonitors mEnteredMonitors;
    private final LockEventListenerIfc mLockEventListener;
    private final LockIdGenerator mLockIdGenerator;
    private final LockingContextIdCache mContextCache;
    private final Logger mLogger;
    private final Counter mNumberOfEnteredMonitors;

    public static EventListener create(Logger logger) throws IOException {
        EventFileWriter eventWriter =
            new EventFileWriter(logger, EVENT_DB_FILE);
        ContextFileWriter contextWriter =
            new ContextFileWriter(logger, CONTEXTS_DB_FILE);
        return new EventListener(logger, eventWriter, contextWriter);
    }

    public EventListener(Logger logger,
                         LockEventListenerIfc lockEventListener,
                         ContextWriterIfc contextWriter) {
        mLogger = logger;
        mEnteredMonitors = new ThreadLocalEnteredMonitors();
        mLockEventListener = lockEventListener;
        mLockIdGenerator = new LockIdGenerator(mLogger, contextWriter);
        mContextCache = new LockingContextIdCache(mLogger, contextWriter);
        mNumberOfEnteredMonitors =
            new Counter("Entered Monitors", mLogger, 100000);
    }

    public synchronized void beforeMonitorEnter(Object monitor,
                                                LockingContext context)
    throws Exception {
        mLogger.finest("EventListener.beforeMonitorEnter");
        mNumberOfEnteredMonitors.increment();
        Iterator<EnteredMonitor> iter = mEnteredMonitors.getIterator();
        while (iter.hasNext()) {
            Object previousEnteredMonitor = iter.next().getMonitorIfStillHeld();
            if (previousEnteredMonitor == null) {
                iter.remove();
            } else if (previousEnteredMonitor == monitor) {
                return; // Monitor already entered.
            }
        }
        enteringNewMonitor(monitor, context);
    }

    private void enteringNewMonitor(Object monitor, LockingContext context)
    throws Exception {
        int newLockId = mLockIdGenerator.acquireLockId(monitor);
        int newContextId = mContextCache.acquireContextId(context);
        EnteredMonitor lastMonitor = mEnteredMonitors.getFirst();
        if (lastMonitor != null) {
            Thread performingThread = Thread.currentThread();
            mLockEventListener.onLockEvent(newLockId,
                                           newContextId,
                                           lastMonitor.getLockId(),
                                           lastMonitor.getLockingContextId(),
                                           performingThread.getId());
        }
        mEnteredMonitors.addFirst(new EnteredMonitor(monitor,
                                                     newLockId,
                                                     newContextId));
    }
}
