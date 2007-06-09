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
