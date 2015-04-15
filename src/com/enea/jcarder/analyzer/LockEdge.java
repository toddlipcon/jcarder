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

import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * A LockEdge instance represents a directed edge from a source LockNode to a
 * target LockNode.
 */
@NotThreadSafe
class LockEdge {
    private final LockNode mSource;
    private final LockNode mTarget;
    private final long mThreadId; // The thread that did the synchronization.
    private int mSourceContextId;
    private int mTargetContextId;
    private long mNumberOfDuplicates;

    private Set<Integer> mGateLockIds;


    LockEdge(LockNode source,
             LockNode target,
             long threadId,
             int sourceLockingContextId,
             int targetLockingContextId) {
        this(source, target, threadId, sourceLockingContextId, targetLockingContextId,
             Collections.<Integer>emptyList());
    }

    LockEdge(LockNode source,
             LockNode target,
             long threadId,
             int sourceLockingContextId,
             int targetLockingContextId,
             Collection<Integer> gateLockIds) {
        mSource = source;
        mTarget = target;
        mThreadId = threadId;
        mSourceContextId = sourceLockingContextId;
        mTargetContextId = targetLockingContextId;
        mNumberOfDuplicates = 0;
        mGateLockIds = new HashSet<Integer>(gateLockIds);
    }

    void merge(LockEdge other) {
        assert this.equals(other);
        mNumberOfDuplicates += (other.mNumberOfDuplicates + 1);
    }

    long getDuplicates() {
        return mNumberOfDuplicates;
    }

    boolean alike(LockEdge other, ContextReaderIfc reader) {
        /*
         * TODO Some kind of cache to improve performance? Note that the context
         * IDs are not declared final.
         */
        LockingContext thisSourceContext =
            reader.readContext(mSourceContextId);
        LockingContext otherSourceContext =
            reader.readContext(other.mSourceContextId);
        LockingContext thisTargetContext =
            reader.readContext(mTargetContextId);
        LockingContext otherTargetContext =
            reader.readContext(other.mTargetContextId);
        return thisSourceContext.alike(otherSourceContext)
               && thisTargetContext.alike(otherTargetContext)
               && mSource.alike(other.mSource, reader)
               && mTarget.alike(other.mTarget, reader);
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
            && (mThreadId == other.mThreadId)
            && (mSourceContextId == other.mSourceContextId)
            && (mTargetContextId == other.mTargetContextId);
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + mSource.getLockId();
        result = prime * result + mSourceContextId;
        result = prime * result + mTarget.getLockId();
        result = prime * result + mTargetContextId;
        result = prime * result + (int) (mThreadId ^ (mThreadId >>> 32));
        return result;
    }

    LockNode getTarget() {
        return mTarget;
    }

    LockNode getSource() {
        return mSource;
    }

    int getSourceLockingContextId() {
        return mSourceContextId;
    }

    int getTargetLockingContextId() {
        return mTargetContextId;
    }

    /**
     * Translate the source and target context ID according to a translation
     * map.
     */
    void translateContextIds(Map<Integer, Integer> translation) {
        final Integer newSourceId = translation.get(mSourceContextId);
        if (newSourceId != null && newSourceId != mSourceContextId) {
            mSourceContextId = newSourceId;
        }
        final Integer newTargetId = translation.get(mTargetContextId);
        if (newTargetId != null && newSourceId != mTargetContextId) {
            mTargetContextId = newTargetId;
        }
    }

    long getThreadId() {
        return mThreadId;
    }

    Set<Integer> getGateLockIds() {
        return mGateLockIds;
    }

    public String toString() {
        return "  " + mSource + "->" + mTarget + "(t " + mThreadId + ")";
    }
}
