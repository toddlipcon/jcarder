package com.enea.jcarder.agent;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.util.Logger;

/**
 * This class is responsible for mapping LockingContext instances to locking
 * context IDs. It maintains a cache to be able to return the same ID again if
 * an ID is requested for the same or equal LockingContext more than once.
 *
 * This class is similar to the java.util.WeakHashMap but uses soft references
 * instead of weak references in order to try to keep the entries in the cache
 * as long as there is enough memory available.
 *
 * TODO An alternative implementation to consider could be to only store the
 * hashCode in a map and perform a comparison with the Context file file
 * (possibly memory mapped). I don't know how that would affect the performance.
 * Another option to consider would be to use a plain HashMap without
 * SoftReferences and accept the potential memory problem as a trade-of for
 * better performance (?) and to avoid getting different IDs for duplicated
 * LockingContexts.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
final class LockingContextIdCache {
    private final HashMap<EqualsComparableKey, Integer> mCache;
    private final ReferenceQueue<Object> mReferenceQueue;
    private final ContextWriterIfc mContextWriter;
    private final Logger mLogger = Logger.getLogger("com.enea.jcarder");

    /**
     * Create a LockingContextIdCache backed by a ContextWriterIfc.
     */
    public LockingContextIdCache(ContextWriterIfc writer) {
        mCache = new HashMap<EqualsComparableKey, Integer>();
        mReferenceQueue = new ReferenceQueue<Object>();
        mContextWriter = writer;
    }

    /**
     * Acquire a unique ID for the provided LockingContext. The ID will be
     * cached. If a provided LockingContext is equal to a previously provided
     * LockingContext that is still in the cache, the same ID will be returned.
     *
     * The equality is checked with the LockingContext.equals(Object other)
     * method.
     */
    public int acquireContextId(LockingContext context) throws IOException {
        assert context != null;
        removeGarbageCollectedKeys();
        Integer id = mCache.get(new StrongKey(context));
        if (id == null) {
            mLogger.finest("Creating new ContextId");
            id = mContextWriter.writeContext(context);
            mCache.put((new SoftKey(context, mReferenceQueue)), id);
        }
        return id;
    }

    private void removeGarbageCollectedKeys() {
        Reference e;
        while ((e = mReferenceQueue.poll()) != null) {
            mLogger.finest("Removing GarbageCollected Cached Context");
            mCache.remove(e);
        }
    }

    private static interface EqualsComparableKey {
        Object get();
        boolean equals(Object obj);
        int hashCode();
    }

    private static class StrongKey implements EqualsComparableKey {
        private final Object mReferent;

        StrongKey(Object referent) {
            assert referent != null;
            mReferent = referent;
        }

        public Object get() {
            return mReferent;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            try {
                EqualsComparableKey reference = (EqualsComparableKey) obj;
                return mReferent.equals(reference.get());
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return mReferent.hashCode();
        }
    }

    private static class SoftKey extends SoftReference<LockingContext>
    implements EqualsComparableKey {
        private final int mHash;

        SoftKey(LockingContext referent, ReferenceQueue<Object> queue) {
            super(referent, queue);
            assert referent != null;
            mHash = referent.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            try {
                Object otherReferent = ((EqualsComparableKey) obj).get();
                Object thisReferent = get();
                return (thisReferent != null
                        && otherReferent != null
                        && thisReferent.equals(otherReferent));
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return mHash;
        }
    }
}
