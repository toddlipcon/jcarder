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

package com.enea.jcarder.common;

import net.jcip.annotations.ThreadSafe;

/**
 * A Lock instance represents a Java monitor object.
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

    public String toString() {
        return mClassName + '@' + Integer.toHexString(mObjectId).toUpperCase();
    }

    public int getObjectId() {
        return mObjectId;
    }

    public String getClassName() {
        return mClassName;
    }

    public int hashCode() {
        return mObjectId;
    }

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
