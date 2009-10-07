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

import java.util.HashMap;
import java.util.Stack;

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
    private HashMap<Integer, LockNode> mLocks =
        new HashMap<Integer, LockNode>();

    private HashMap<Long, HashMap<Integer, LockWithContext>> currentLocksByThread =
        new HashMap<Long, HashMap<Integer, LockWithContext>>();


    LockNode getLockNode(int lockId) {
        LockNode lockNode = mLocks.get(lockId);
        if (lockNode == null) {
            lockNode = new LockNode(lockId);
            mLocks.put(lockId, lockNode);
        }
        return lockNode;
    }

    public void onLockEvent(boolean isLock,
                            int lockId,
                            int lockingContextId,
                            long threadId) {
        /*
        System.err.println("thr " + threadId +
                           "\tisLock: " + isLock + "\tlock: " + lockId + "\tctx: " + lockingContextId);
        */
        HashMap<Integer, LockWithContext> heldLocks = currentLocksByThread.get(threadId);
        if (heldLocks == null) {
            heldLocks = new HashMap<Integer, LockWithContext>();
            currentLocksByThread.put(threadId, heldLocks);
        }

        if (isLock) {
            // If we've already locked this monitor, just up the refcount
            LockWithContext alreadyHeld = heldLocks.get(lockId);
            if (alreadyHeld != null) {
                alreadyHeld.refCount++;
                return;
            }

            final LockNode targetLock = getLockNode(lockId);

            // This must be a new lock.
            // Add graph edges from all currently held locks to this one
            for (LockWithContext sourceLwc : heldLocks.values()) {
                LockNode sourceLock = getLockNode(sourceLwc.nodeId);
                final LockEdge edge = new LockEdge(sourceLock,
                                                   targetLock,
                                                   threadId,
                                                   sourceLwc.contextId,
                                                   lockingContextId,
                                                   heldLocks.keySet());
                sourceLock.addOutgoingEdge(edge);
            }
            // And add this one to the set

            heldLocks.put(lockId, new LockWithContext(lockId, lockingContextId));
        } else {
            // We should find it there
            LockWithContext alreadyHeld = heldLocks.get(lockId);
            if (alreadyHeld == null) {
                throw new RuntimeException("Unlocking unheld lock!");
            }
            if (--alreadyHeld.refCount == 0) {
                heldLocks.remove(lockId);
            }
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
