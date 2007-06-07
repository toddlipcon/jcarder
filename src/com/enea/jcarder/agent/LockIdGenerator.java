package com.enea.jcarder.agent;

import java.io.IOException;
import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.util.IdentityWeakHashMap;
import com.enea.jcarder.util.Logger;

/**
 * This class is responsible for generating unique ids for objects.
 *
 * We can not use System.identityHashCode(o) since it returns random numbers
 * which is not guaranteed to be unique.
 *
 * This class is using a RandomAccessStoreWriterIfc as a backend for storing
 * the ids.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
public final class LockIdGenerator implements LockIdAcquiringIfc {
    private final IdentityWeakHashMap<Integer> mHashMap;
    private final ContextWriterIfc mRas;
    private final Logger mLogger = Logger.getLogger("com.enea.jcarder");

    public LockIdGenerator(ContextWriterIfc ras) {
        mHashMap = new IdentityWeakHashMap<Integer>();
        mRas = ras;
    }

    /*
     * Return an id for a given object.
     *
     * If the method is invoked witht the same object instance more than once
     * this method will guarantee that they will get the same id. Two objects
     * that are not identical (as compared with "==") will get different ids.
     */
    public int acquireLockId(Object o) throws IOException {
        assert o != null;
        Integer id = mHashMap.get(o);
        if (id == null) {
            id = mRas.writeLock(new Lock(o));
            mHashMap.put(o, id);
            mLogger.finest("Created new LockId: " + id);
        }
        return id;
    }
}
