package com.enea.jcarder.agent;

import java.io.IOException;
import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.util.IdentityWeakHashMap;
import com.enea.jcarder.util.logging.Logger;

/**
 * This class is responsible for generating unique IDs for objects.
 *
 * We cannot use System.identityHashCode(o) since it returns random numbers,
 * which are not guaranteed to be unique.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
final class LockIdGenerator {
    private final IdentityWeakHashMap<Integer> mIdMap;
    private final ContextWriterIfc mContextWriter;
    private final Logger mLogger;

    /**
     * Create a LockIdGenerator backed by a ContextWriterIfc
     */
    public LockIdGenerator(Logger logger, ContextWriterIfc writer) {
        mLogger = logger;
        mIdMap = new IdentityWeakHashMap<Integer>();
        mContextWriter = writer;
    }

    /**
     * Return an ID for a given object.
     *
     * If the method is invoked with the same object instance more than once it
     * is guaranteed that the same ID is returned each time. Two objects that
     * are not identical (as compared with "==") will get different IDs.
     */
    public int acquireLockId(Object o) throws IOException {
        assert o != null;
        Integer id = mIdMap.get(o);
        if (id == null) {
            id = mContextWriter.writeLock(new Lock(o));
            mIdMap.put(o, id);
            mLogger.finest("Created new lock ID: " + id);
        }
        return id;
    }
}
