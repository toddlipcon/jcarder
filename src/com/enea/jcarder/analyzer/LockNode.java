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
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * A LockNode instance represents a lock in a graph.
 */
@NotThreadSafe
class LockNode {
    enum CycleType { NO_CYCLE, SINGLE_THREADED_CYCLE, CYCLE };

    private final int mLockId;
    private Map<LockEdge, LockEdge> mOutgoingEdges;
    private CycleType mCycleType = CycleType.NO_CYCLE;

    LockNode(final int lockId) {
        mLockId = lockId;
        mOutgoingEdges = new HashMap<LockEdge, LockEdge>();
    }

    int getLockId() {
        return mLockId;
    }

    CycleType getCycleType() {
        return mCycleType;
    }

    void raiseCycleType(CycleType newCycleType) {
        if (newCycleType.compareTo(mCycleType) > 0) {
            mCycleType = newCycleType;
        }
    }

    void addOutgoingEdge(LockEdge newEdge) {
        LockEdge existingEdge = mOutgoingEdges.get(newEdge);
        if (existingEdge == null) {
            mOutgoingEdges.put(newEdge, newEdge);
        } else {
            existingEdge.merge(newEdge);
        }
    }

    void populateContextIdTranslationMap(Map<Integer, Integer> translationMap) {
        for (LockEdge edge : mOutgoingEdges.values()) {
            translationMap.put(edge.getSourceLockingContextId(),
                               edge.getSourceLockingContextId());
            translationMap.put(edge.getTargetLockingContextId(),
                               edge.getTargetLockingContextId());
        }
    }

    void translateContextIds(Map<Integer, Integer> translation) {
        Map<LockEdge, LockEdge> oldEdges = mOutgoingEdges;
        mOutgoingEdges = new HashMap<LockEdge, LockEdge>(oldEdges.size());
        for (LockEdge edge : oldEdges.values()) {
            edge.translateContextIds(translation);
            addOutgoingEdge(edge);
        }
    }

    Set<LockEdge> getOutgoingEdges() {
        return mOutgoingEdges.keySet();
    }

    public String toString() {
        return "L_" + mLockId;
    }

    long numberOfUniqueEdges() {
        return mOutgoingEdges.size();
    }

    long numberOfDuplicatedEdges() {
        long numberOfDuplicatedEdges = 0;
        for (LockEdge edge : mOutgoingEdges.values()) {
            numberOfDuplicatedEdges += edge.getDuplicates();
        }
        return numberOfDuplicatedEdges;
    }

    boolean alike(LockNode other, ContextReaderIfc reader) {
        // TODO Maybe introduce some kind of cache to improve performance?
        String thisClassName = reader.readLock(mLockId).getClassName();
        String otherClassName = reader.readLock(other.mLockId).getClassName();
        return thisClassName.equals(otherClassName);
    }
}
