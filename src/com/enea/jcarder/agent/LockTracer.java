package com.enea.jcarder.agent;

import java.util.concurrent.locks.Lock;

public abstract class LockTracer {
  public static void lock(Lock l, String refName, String stack) {
    System.err.println("Tracing lock of " + l + " (" + refName + ") at " + stack);
    StaticEventListener.beforeMonitorEnter(l, refName, stack);
    l.lock();
  }

  public static void unlock(Lock l, String refName, String stack) {
    System.err.println("Tracing unlock of " + l + " (" + refName + ") at " + stack);
    StaticEventListener.beforeMonitorExit(l, refName, stack);
    l.unlock();
  }

}
