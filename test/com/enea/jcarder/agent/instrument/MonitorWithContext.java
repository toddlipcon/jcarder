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

package com.enea.jcarder.agent.instrument;

import com.enea.jcarder.common.LockingContext;

public final class MonitorWithContext {
    private final Object mMonitor;
    private final LockingContext mContext;

    public MonitorWithContext(Object monitor,
                       LockingContext context) {
        mMonitor = monitor;
        mContext = context;
    }

    public MonitorWithContext(Object monitor,
                              String method,
                              String lockReference,
                              Thread thread) {
        mMonitor = monitor;
        mContext = new LockingContext(thread, lockReference, method);
    }

    public static MonitorWithContext[] create(Object monitor,
                                              String method,
                                              String lockReference) {
        MonitorWithContext context =
            new MonitorWithContext(monitor,
                                   method,
                                   lockReference,
                                 Thread.currentThread());
        return new MonitorWithContext[] { context };

    }

    public static MonitorWithContext[] create(Object monitor,
                                              Class<?> baseClass,
                                              String methodShortName,
                                              String lockReference,
                                              int lineNumber) {
        MonitorWithContext context =
            new MonitorWithContext(monitor,
                baseClass.getName() + "." + methodShortName+ "() " +
                    "(" + baseClass.getSimpleName() + ".java:" + lineNumber + ")",
                lockReference,
                Thread.currentThread());
        return new MonitorWithContext[] { context };

    }

    /**
     * Auto-generated by Eclipse.
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        int contextHash = this.mContext == null ? 0 : this.mContext.hashCode();
        result = PRIME * result + contextHash;
        int monitorHash = this.mMonitor == null ? 0 : this.mMonitor.hashCode();
        result = PRIME * result + monitorHash;
        return result;
    }

    /**
     * Auto-generated by Eclipse.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MonitorWithContext other = (MonitorWithContext) obj;
        if (this.mContext == null) {
            if (other.mContext != null)
                return false;
        } else if (!this.mContext.equals(other.mContext))
            return false;
        if (this.mMonitor == null) {
            if (other.mMonitor != null)
                return false;
        } else if (!this.mMonitor.equals(other.mMonitor))
            return false;
        return true;
    }

    public String toString() {
        return "Monitor: " + System.identityHashCode(mMonitor)
                + " " + mContext.toString();
    }

    public LockingContext getContext() {
        return this.mContext;
    }
}
