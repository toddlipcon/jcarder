/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.analyzer;

import com.enea.jcarder.common.contexts.ContextReaderIfc;
import com.enea.jcarder.util.logging.Logger;
import java.util.HashMap;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.events.LockEventListenerIfc;

/**
 * This class is responsible for constructing a structure of LockNode and
 * LockEdge objects from incoming lock events.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
class LockGraphBuilder implements LockEventListenerIfc {

    private final Logger mLogger;
    private final ContextReaderIfc mContextReader;

    private HashMap<Integer, LockNode> mLocks =
        new HashMap<Integer, LockNode>();

    private HashMap<Long, HashMap<Integer, LockWithContext>> currentLocksByThread =
        new HashMap<Long, HashMap<Integer, LockWithContext>>();

    public LockGraphBuilder(Logger logger, ContextReaderIfc contextReader) {
        mLogger = logger;
        mContextReader = contextReader;
    }

    LockNode getLockNode(int lockId) {
        LockNode lockNode = mLocks.get(lockId);
        if (lockNode == null) {
            lockNode = new LockNode(lockId);
            mLocks.put(lockId, lockNode);
        }
        return lockNode;
    }

    public void onLockEvent(LockEventType eventType,
                            int lockId,
                            int lockingContextId,
                            long threadId) {
        HashMap<Integer, LockWithContext> heldLocks =
            currentLocksByThread.get(threadId);
        if (heldLocks == null) {
            heldLocks = new HashMap<Integer, LockWithContext>();
            currentLocksByThread.put(threadId, heldLocks);
        }

        if (eventType == LockEventType.MONITOR_ENTER ||
            eventType == LockEventType.LOCK_LOCK ||
            eventType == LockEventType.SHARED_LOCK_LOCK) {

            if (eventType == LockEventType.SHARED_LOCK_LOCK) {
                // System.err.println("SHARED LOCK");
                lockId = -lockId;
            }

            // If we've already locked this monitor, just up the refcount.
            LockWithContext alreadyHeld = heldLocks.get(lockId);
            if (alreadyHeld != null) {
                alreadyHeld.refCount++;
                return;
            }

            // Verify that the lock and context are both in the context DB.
            // They may not be if the end of the file was corrupted during shutdown.
            if (mContextReader != null) {
                try {
                    mContextReader.readLock(lockId);
                } catch (RuntimeException e) {
                    mLogger.warning("Cannot find lock ID " + lockId + " in database. Ignoring.");
                    return;
                }
                try {
                    mContextReader.readContext(lockingContextId);
                } catch (RuntimeException e) {
                    mLogger.warning("Cannot find context ID " + lockingContextId + " in database. Ignoring.");
                    return;
                }
            }

            final LockNode targetLock = getLockNode(lockId);

            // This must be a new lock. Add graph edges from all currently held
            // locks to this one.
            for (LockWithContext sourceLwc : heldLocks.values()) {
                LockNode sourceLock = getLockNode(sourceLwc.nodeId);
                final LockEdge edge = new LockEdge(sourceLock, targetLock);
                final LockTransition transition = new LockTransition(
                        threadId,
                        sourceLwc.contextId,
                        lockingContextId,
                        heldLocks.keySet()
                );
                sourceLock.addOutgoingEdge(edge)
                        .addTransition(transition);
            }

            // And add this one to the set.
            heldLocks.put(lockId,
                          new LockWithContext(lockId, lockingContextId));
        } else if (eventType == LockEventType.MONITOR_EXIT ||
                   eventType == LockEventType.LOCK_UNLOCK ||
                   eventType == LockEventType.SHARED_LOCK_UNLOCK) {

            if (eventType == LockEventType.SHARED_LOCK_UNLOCK) {
                lockId = -lockId;
            }

            // We should find it there unless the end of the DB was corrupted.
            LockWithContext alreadyHeld = heldLocks.get(lockId);
            if (alreadyHeld == null) {
                mLogger.warning("Cannot find held lock ID " + lockId + ". Ignoring.");
                return;
            }
            --alreadyHeld.refCount;
            if (alreadyHeld.refCount == 0) {
                heldLocks.remove(lockId);
            }
        } else {
            throw new RuntimeException("Unknown lock event type: " + eventType);
        }
    }

    void clear() {
        mLocks.clear();
    }

    Iterable<LockNode> getAllLocks() {
        return mLocks.values();
    }

    private static class LockWithContext {
        public final int nodeId;
        public final int contextId;

        public int refCount = 1;

        public LockWithContext(int nodeId, int contextId) {
            this.nodeId = nodeId;
            this.contextId = contextId;
        }
    }
}
