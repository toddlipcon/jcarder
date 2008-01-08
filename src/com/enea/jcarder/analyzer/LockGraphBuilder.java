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

    LockNode getLockNode(int lockId) {
        LockNode lockNode = mLocks.get(lockId);
        if (lockNode == null) {
            lockNode = new LockNode(lockId);
            mLocks.put(lockId, lockNode);
        }
        return lockNode;
    }

    public void onLockEvent(int lockId,
                            int lockingContextId,
                            int lastTakenLockId,
                            int lastTakenLockingContectId,
                            long threadId) {
        if (lastTakenLockId >= 0) {
            final LockNode sourceLock = getLockNode(lastTakenLockId);
            final LockNode targetLock = getLockNode(lockId);
            final LockEdge edge = new LockEdge(sourceLock,
                                               targetLock,
                                               threadId,
                                               lastTakenLockingContectId,
                                               lockingContextId);
            sourceLock.addOutgoingEdge(edge);
        }
    }

    void clear() {
        mLocks.clear();
    }

    Iterable<LockNode> getAllLocks() {
        return mLocks.values();
    }
}
