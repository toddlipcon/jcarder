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

package com.enea.jcarder.agent;

import java.lang.ref.WeakReference;

/**
 * Each instance of this class represents an entered monitor.
 *
 * The reference to the monitor object is kept as a WeakReference internally in
 * this class and won't prevent the monitor from being garbage collected.
 *
 * TODO Add basic test for the WeakReference handling.
 */
final class EnteredMonitor {
    private final WeakReference mMonitorRef;
    private final int mLockingContextId;
    private final int mLockId;

    EnteredMonitor(Object monitor,
                   int lockId,
                   int lockingContextId) {
        mLockId = lockId;
        mLockingContextId = lockingContextId;
        mMonitorRef = new WeakReference<Object>(monitor);
    }

    Object getMonitorIfStillHeld() {
        final Object monitor = mMonitorRef.get();
        /*
         * TODO The call to Thread.holdsLock takes long time. It would be
         * interesting to test how the performance would be affected if this
         * code is removed and replaced by an event for each MonitorExit and
         * each finished synchronized method.
         */
        if (monitor != null && Thread.holdsLock(monitor)) {
            return monitor;
        } else {
            return null;
        }
    }

    int getLockingContextId() {
        return mLockingContextId;
    }

    int getLockId() {
        return mLockId;
    }
}
