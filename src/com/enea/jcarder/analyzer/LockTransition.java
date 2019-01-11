package com.enea.jcarder.analyzer;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextReaderIfc;
import net.jcip.annotations.NotThreadSafe;

import java.util.*;

/**
 * A lock transition represents transition from source lock to target
 */
@NotThreadSafe
class LockTransition {
    private final long mThreadId; // The thread that did the synchronization.
    private int mSourceContextId;
    private int mTargetContextId;
    private long mNumberOfDuplicates;

    private Set<Integer> mGateLockIds;

    LockTransition(long threadId,
             int sourceLockingContextId,
             int targetLockingContextId) {
        this(threadId, sourceLockingContextId, targetLockingContextId,
             Collections.emptyList());
    }

    LockTransition(long threadId,
             int sourceLockingContextId,
             int targetLockingContextId,
             Collection<Integer> gateLockIds) {
        mThreadId = threadId;
        mSourceContextId = sourceLockingContextId;
        mTargetContextId = targetLockingContextId;
        mNumberOfDuplicates = 0;
        mGateLockIds = new HashSet<>(gateLockIds);
    }

    void merge(LockTransition other) {
        assert this.equals(other);
        mNumberOfDuplicates += (other.mNumberOfDuplicates + 1);
    }

    long getDuplicates() {
        return mNumberOfDuplicates;
    }

    boolean alike(LockTransition other, ContextReaderIfc reader) {
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
               && thisTargetContext.alike(otherTargetContext);
    }

    public boolean equals(Object obj) {
        try {
            LockTransition other = (LockTransition) obj;
            return (mThreadId == other.mThreadId)
            && (mSourceContextId == other.mSourceContextId)
            && (mTargetContextId == other.mTargetContextId);
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = prime * mSourceContextId;
        result = prime * result + mTargetContextId;
        result = prime * result + (int) (mThreadId ^ (mThreadId >>> 32));
        return result;
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
        return "(t " + mThreadId + ")";
    }
}
