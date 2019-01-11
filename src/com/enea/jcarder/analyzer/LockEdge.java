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

import java.util.*;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * A LockEdge instance represents a directed edge from a source LockNode to a
 * target LockNode.
 */
@NotThreadSafe
class LockEdge {
    private final LockNode mSource;
    private final LockNode mTarget;
    private final LockTransition mTransition;

    // full mode constructor
    LockEdge(LockNode source, LockNode target, LockTransition transition) {
        mSource = source;
        mTarget = target;
        mTransition = transition;
    }

    Collection<LockTransition> getTransitions() {
        return Collections.singletonList(mTransition);
    }

    void merge(LockEdge other) {
        assert this.equals(other);
        mTransition.merge(other.mTransition);
    }


    long numberOfUniqueTransitions() {
        return 1;
    }

    long numberOfDuplicatedTransitions() {
        return mTransition.getDuplicates();
    }

    void populateContextIdTranslationMap(Map<Integer, Integer> translationMap) {
        translationMap.put(mTransition.getSourceLockingContextId(),
                mTransition.getSourceLockingContextId());
        translationMap.put(mTransition.getTargetLockingContextId(),
                mTransition.getTargetLockingContextId());
    }

    void translateContextIds(Map<Integer, Integer> translation) {
        mTransition.translateContextIds(translation);
    }

    void removeAlikeTransitions(ContextReaderIfc reader) {
    }

    boolean alike(LockEdge other, ContextReaderIfc reader) {
        return mSource.alike(other.mSource, reader)
                && mTarget.alike(other.mTarget, reader)
                && mTransition.alike(other.mTransition, reader);
    }

    public boolean equals(Object obj) {
        /*
         * TODO It might be a potential problem to use LockEdges in HashMaps
         * since they are mutable and this equals method depends on them?
         */
        try {
            LockEdge other = (LockEdge) obj;
            return (mTarget.getLockId() == other.mTarget.getLockId())
            && (mSource.getLockId() == other.mSource.getLockId())
            && (mTransition.equals(other.mTransition))
            ;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = prime + mSource.getLockId();
        result = prime * result + mTarget.getLockId();
        result = prime * result + mTransition.hashCode();
        return result;
    }

    LockNode getTarget() {
        return mTarget;
    }

    LockNode getSource() {
        return mSource;
    }

    long getThreadId() {
        return mTransition.getThreadId();
    }

    boolean hasThreadId(long threadId) {
        return mTransition.getThreadId() == threadId;
    }

    Set<Integer> getGateLockIds() {
        return mTransition.getGateLockIds();
    }

    public String toString() {
        return "  " + mSource + "->" + mTarget + "(t " + mTransition.getThreadId() + ")";
    }
}
