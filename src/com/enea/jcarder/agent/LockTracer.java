package com.enea.jcarder.agent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;
import java.lang.reflect.Field;


public abstract class LockTracer {
  static Field reentrantLockSync;
  static Field rrwLockReadLockSync;
  static Field rrwLockWriteLockSync;

  static {
    try {
      reentrantLockSync = ReentrantLock.class.getDeclaredField("sync");
      reentrantLockSync.setAccessible(true);
      rrwLockReadLockSync = ReentrantReadWriteLock.ReadLock.class.getDeclaredField("sync");
      rrwLockReadLockSync.setAccessible(true);
      rrwLockWriteLockSync = ReentrantReadWriteLock.WriteLock.class.getDeclaredField("sync");
      rrwLockWriteLockSync.setAccessible(true);
    } catch (NoSuchFieldException nsfe) {
      throw new RuntimeException(nsfe);
    }
  }

  private static Object getSyncObject(Lock l) {
    Field fieldToGrab = null;
    if (l instanceof ReentrantLock) {
      fieldToGrab = reentrantLockSync;
    } else if (l instanceof ReentrantReadWriteLock.ReadLock) {
      fieldToGrab = rrwLockReadLockSync;
    } else if (l instanceof ReentrantReadWriteLock.WriteLock) {
      fieldToGrab = rrwLockWriteLockSync;
    }


    if (fieldToGrab == null) return l;
    try {
      return fieldToGrab.get(l);
    } catch (IllegalAccessException iae) {
      return l;
    }
  }

  public static void lock(Lock l, String refName, String stack) {
    // System.err.println("Tracing lock of " + l + " (" + refName + ") at " + stack);
    recordLock(l, refName, stack);
    l.lock();
  }

  public static void unlock(Lock l, String refName, String stack) {
    // System.err.println("Tracing unlock of " + l + " (" + refName + ") at " + stack);

    if (l instanceof ReentrantReadWriteLock.ReadLock) {
      StaticEventListener.handleEvent(
        LockEventType.SHARED_LOCK_UNLOCK, getSyncObject(l), refName, stack);
    } else if (l instanceof ReentrantReadWriteLock.WriteLock) {
      StaticEventListener.handleEvent(
        LockEventType.SHARED_LOCK_UNLOCK, getSyncObject(l), refName, stack);
      StaticEventListener.handleEvent(
        LockEventType.LOCK_UNLOCK, getSyncObject(l), refName, stack);
    } else {
      StaticEventListener.handleEvent(
        LockEventType.LOCK_UNLOCK, getSyncObject(l), refName, stack);
    }

    l.unlock();
  }

  public static boolean tryLock(Lock l, String refName, String stack) {
    // What to do here is a bit unclear.
    // We have two options:
    //   a) only record the lock if it's successful (the safe route)
    //   b) if not successful, record it as a lock and immediate unlock
    //   c) record a special trylock event type and make the analyzer figure it out
    // I've elected for (a) since it's simplest.
    boolean ret = l.tryLock();
    if (!ret) {
      return ret;
    }

    // If we succeeded in locking, we should trace it like a normal lock
    recordLock(l, refName, stack);
    return ret;
  }

  public static void lockInterruptibly(Lock l, String refName, String stack) throws InterruptedException {
    try {
      l.lockInterruptibly();
      recordLock(l, refName, stack);
    } catch (InterruptedException e) {
      throw e;
    }
  }

  private static void recordLock(Lock l, String refName, String stack) {
    if (l instanceof ReentrantReadWriteLock.ReadLock) {
      StaticEventListener.handleEvent(
        LockEventType.SHARED_LOCK_LOCK, getSyncObject(l), refName, stack);
    } else if (l instanceof ReentrantReadWriteLock.WriteLock) {
      StaticEventListener.handleEvent(
        LockEventType.SHARED_LOCK_LOCK, getSyncObject(l), refName, stack);
      StaticEventListener.handleEvent(
        LockEventType.LOCK_LOCK, getSyncObject(l), refName, stack);
    } else {
      StaticEventListener.handleEvent(
        LockEventType.LOCK_LOCK, getSyncObject(l), refName, stack);
    }
  }
}
