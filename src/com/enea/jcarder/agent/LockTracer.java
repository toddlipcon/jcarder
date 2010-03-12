package com.enea.jcarder.agent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;
import java.lang.reflect.Field;


public abstract class LockTracer {
  static Field reentrantLockSync;

  static {
    try {
      reentrantLockSync = ReentrantLock.class.getDeclaredField("sync");
      reentrantLockSync.setAccessible(true);
    } catch (NoSuchFieldException nsfe) {
      throw new RuntimeException(nsfe);
    }
  }

  private static Object getSyncObject(Lock l) {
    if (l instanceof ReentrantLock) {
      try {
        return reentrantLockSync.get(l);
      } catch (IllegalAccessException iae) {
        return l;
      }
    } else {
      return l;
    }
  }

  public static void lock(Lock l, String refName, String stack) {
    System.err.println("Tracing lock of " + l + " (" + refName + ") at " + stack);
    StaticEventListener.handleEvent(
      LockEventType.LOCK_LOCK, getSyncObject(l), refName, stack);
    l.lock();
  }

  public static void unlock(Lock l, String refName, String stack) {
    System.err.println("Tracing unlock of " + l + " (" + refName + ") at " + stack);
    StaticEventListener.handleEvent(
      LockEventType.LOCK_UNLOCK, getSyncObject(l), refName, stack);
    l.unlock();
  }

}
