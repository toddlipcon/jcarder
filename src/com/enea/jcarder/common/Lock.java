package com.enea.jcarder.common;

import net.jcip.annotations.ThreadSafe;

/**
 * A Lock instance represents a java monitor object.
 */
@ThreadSafe
public final class Lock {
    private final String mClassName;
    private final int mObjectId;

    public Lock(Object lock) {
        mClassName = lock.getClass().getName();
        mObjectId = System.identityHashCode(lock);
    }

    public Lock(String className, int objectId) {
        mClassName = className;
        mObjectId = objectId;
    }

    @Override
    public String toString() {
        return mClassName + '@' + Integer.toHexString(mObjectId).toUpperCase();
    }

    public int getObjectId() {
        return mObjectId;
    }

    public String getClassName() {
        return mClassName;
    }

    @Override
    public int hashCode() {
        return mObjectId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Lock other = (Lock) obj;
        return mObjectId == other.mObjectId
               && mClassName.equals(other.mClassName);
    }
}
