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

import net.jcip.annotations.ThreadSafe;
import com.enea.jcarder.common.LockingContext;

/**
 * This class provides static methods that are supposed to be invoked directly
 * from the instrumented classes.
 */
@ThreadSafe
public final class StaticEventListener {

    private StaticEventListener() { }
    private static EventListenerIfc smListener;

    public synchronized static void setListener(EventListenerIfc listener) {
        smListener = listener;
    }

    public synchronized static EventListenerIfc getListener() {
        return smListener;
    }

    /**
     * This method is expected to be called from the instrumented classes.
     *
     * @param monitor
     *            The monitor object that was acquired. This value is allowed to
     *            be null.
     *
     * @param lockReference
     *            A textual description of how the lock object was addressed.
     *            For example: "this", "com.enea.jcarder.Foo.mBar" or
     *            "com.enea.jcarder.Foo.getLock()".
     *
     * @param methodWithClass
     *            The method that acquired the lock, on the format
     *            "com.enea.jcarder.Foo.bar()".
     */
    public static void beforeMonitorEnter(Object monitor,
                                          String lockReference,
                                          String methodWithClass) {
        try {
            EventListenerIfc listener = getListener();
            if (listener != null) {
                final LockingContext lockingContext =
                    new LockingContext(Thread.currentThread(),
                                       lockReference,
                                       methodWithClass);
                listener.beforeMonitorEnter(monitor,
                                            lockingContext);
            }
        } catch (Throwable t) {
            handleError(t);
        }
    }

    private static void handleError(Throwable t) {
        setListener(null);
        t.printStackTrace();
    }
}
