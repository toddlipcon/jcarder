package com.enea.jcarder.agent;

import com.enea.jcarder.common.LockingContext;

/**
 * This class provides static methods that are supposed to be invoked directly
 * from the instrumented classes.
 */
public final class StaticEventListener {

    private StaticEventListener() { }
    private static EventListenerIfc smListener;

    public static void setListener(EventListenerIfc listener) {
        smListener = listener;
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
            if (smListener != null) {
                final LockingContext lockingContext =
                    new LockingContext(Thread.currentThread(),
                                       lockReference,
                                       methodWithClass);
                smListener.beforeMonitorEnter(monitor,
                                              lockingContext);
            }
        } catch (Throwable t) {
            handleError(t);
        }
    }

    private static void handleError(Throwable t) {
        smListener = null;
        t.printStackTrace();
    }
}
