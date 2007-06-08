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
 * which are not guaranteed to be unique.
 * 
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
final class LockIdGenerator {
    private final IdentityWeakHashMap<Integer> mIdMap;
    private final ContextWriterIfc mContextWriter;
    private final Logger mLogger = Logger.getLogger("com.enea.jcarder");

    /**
     * Create a LockIdGenerator backed by a ContextWriterIfc
     */
    public LockIdGenerator(ContextWriterIfc writer) {
        mIdMap = new IdentityWeakHashMap<Integer>();
        mContextWriter = writer;
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
        Integer id = mIdMap.get(o);
        if (id == null) {
            id = mContextWriter.writeLock(new Lock(o));
            mIdMap.put(o, id);
            mLogger.finest("Created new LockId: " + id);
        }
        return id;
    }
}
