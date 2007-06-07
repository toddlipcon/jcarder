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
 * This class is responsible for mapping LockingContext instances to
 * locking context ids. It maintains a cache to be able to return
 * the same id again if an id is requested for the same or equal LockingContext,
 * more than once.
 *
 * This class is similar to the java.util.WeakHashMap but uses soft references
 * instead of weak references in order to try to keep the entries in the cache
 * as long as there is enough memory available.
 *
 * This class is using a RandomAccessStoreWriterIfc as a backend for storing
 * the ids.
 *
 * TODO An alternative implementation to consider could be to only store the
 *      hashCode in a map and perform a comparison with the RandomAccessStore
 *      file (which can be expected to be memory mapped). I don't know how
 *      that would affect the performance. Another option  to consider would be
 *      to use a plain HashMap without SoftReferences and accept the potential
 *      memory problem as a trade of for better performance (?) and to avoid
 *      getting different ids for duplicated LockingContexts.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
public final class LockingContextIdCache implements LockingContextAcquiringIfc {
    private final HashMap<EqualsComparableKey, Integer> mHashMap;
    private final ReferenceQueue<Object> mReferenceQueue;
    private final ContextWriterIfc mRas;
    private final Logger mLogger = Logger.getLogger("com.enea.jcarder");

    public LockingContextIdCache(ContextWriterIfc ras) {
        mHashMap = new HashMap<EqualsComparableKey, Integer>();
        mReferenceQueue = new ReferenceQueue<Object>();
        mRas = ras;
    }

    /**
     * Acquire an unique id for the provided LockingContext. The id will be
     * chached. If a provided LockingContext is equal to a previously
     * provided LockingContext that is still in the cache, the same id will be
     * returned.
     *
     * The equality is checked with the LockingContext.equals(Object other)
     * method.
     */
    public int acquireLockingContextId(LockingContext lockingContext)
    throws IOException {
        assert lockingContext != null;
        removeGarbageCollectedKeys();
        Integer id = mHashMap.get(new StrongKey(lockingContext));
        if (id == null) {
            mLogger.finest("Creating new ContextId");
            id = mRas.writeLockingContext(lockingContext);
            mHashMap.put((new SoftKey(lockingContext, mReferenceQueue)), id);
        }
        return id;
    }

    private void removeGarbageCollectedKeys() {
        Reference e;
        while ((e = mReferenceQueue.poll()) != null) {
            mLogger.finest("Removing GarbageCollected Cached Context");
            mHashMap.remove(e);
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
