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

import java.util.ArrayList;
import java.util.Iterator;
import net.jcip.annotations.ThreadSafe;

/**
 * Each instance of this class keeps a list of entered monitors for a thread.
 *
 * Note that this class is a ThreadLocal and therefore each thread will have its
 * own instance.
 */

@ThreadSafe
final class ThreadLocalEnteredMonitors
extends ThreadLocal<ArrayList<EnteredMonitor>> {

    public ArrayList<EnteredMonitor> initialValue() {
        return new ArrayList<EnteredMonitor>();
    }

    Iterator<EnteredMonitor> getIterator() {
        return get().iterator();
    }

    EnteredMonitor getFirst() {
        ArrayList<EnteredMonitor> list = get();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    void addFirst(EnteredMonitor enteredMonitor) {
        get().add(0, enteredMonitor);
    }
}
