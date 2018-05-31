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

import java.util.concurrent.locks.ReentrantLock;
import com.enea.jcarder.agent.StaticEventListener;

public class TracingReentrantLock extends ReentrantLock {
  private final String creationInfo;
  
  /**
   * An Object used to track through the other layers of
   * JCarder for the purpose of unique lock ID generation.
   *
   * This is important 
   */
  private final ReentrantLockStandin lockStandin;

  public TracingReentrantLock(String creationInfo) {
    super();
    this.creationInfo = creationInfo;
    this.lockStandin = new ReentrantLockStandin();
  }

  public TracingReentrantLock(boolean fair, String creationInfo) {
    super(fair);
    this.creationInfo = creationInfo;
    this.lockStandin = new ReentrantLockStandin();
  }

  @Override
  public void lock() {
    System.err.println("Locking " + this + " (" + creationInfo + ")");
    StaticEventListener.beforeMonitorEnter(lockStandin, creationInfo, getCallSite());
    super.lock();
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    System.err.println("lockInterruptibly " + this);
    StaticEventListener.beforeMonitorEnter(lockStandin, creationInfo, getCallSite());
    try {
      super.lockInterruptibly();
    } catch (InterruptedException ie) {
      // did not acquire the lock
      StaticEventListener.beforeMonitorExit(lockStandin, creationInfo, getCallSite());
    }
  }

  @Override
  public boolean tryLock() {
    boolean ret = super.tryLock();
    if (ret) {
      StaticEventListener.beforeMonitorEnter(lockStandin, creationInfo, getCallSite());
    }
    return ret;
  }

  @Override
  public void unlock() {
    System.err.println("Unlocking " + this);
    StaticEventListener.beforeMonitorExit(lockStandin, creationInfo, getCallSite());
    super.unlock();
  }


  private String getCallSite() {
    StackTraceElement elems[] = Thread.currentThread().getStackTrace();
    if (elems.length < 4) {
      return "<unknown>";
    }

    StackTraceElement caller = elems[3];
    return caller.toString();
  }

  // TODO(tlipcon) any other lock methods?

  private static class ReentrantLockStandin {}
}
