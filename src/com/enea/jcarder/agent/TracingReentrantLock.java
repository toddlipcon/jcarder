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

public class TracingReentrantLock extends ReentrantLock {
  private final String creationInfo;

  public TracingReentrantLock(String creationInfo) {
    super();
    this.creationInfo = creationInfo;
  }

  public TracingReentrantLock(boolean fair, String creationInfo) {
    super(fair);
    this.creationInfo = creationInfo;
  }

  @Override
  public void lock() {
    System.err.println("Locking " + this + " (" + creationInfo + ")");
    super.lock();
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    System.err.println("lockInterruptibly " + this);
    super.lockInterruptibly();
  }

  @Override
  public void unlock() {
    System.err.println("Unlocking " + this);
    super.unlock();
  }
}
