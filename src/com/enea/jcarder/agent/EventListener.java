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

import static com.enea.jcarder.common.contexts.ContextFileReader.EVENT_DB_FILENAME;
import static com.enea.jcarder.common.contexts.ContextFileReader.CONTEXTS_DB_FILENAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextFileWriter;
import com.enea.jcarder.common.contexts.ContextWriterIfc;
import com.enea.jcarder.common.events.EventFileWriter;
import com.enea.jcarder.common.events.LockEventListenerIfc;
import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.logging.Logger;

@ThreadSafe
final class EventListener implements EventListenerIfc {
    private final LockEventListenerIfc mLockEventListener;
    private final LockIdGenerator mLockIdGenerator;
    private final LockingContextIdCache mContextCache;
    private final Logger mLogger;
    private final Counter mNumberOfEnteredMonitors;

    private final Map<Class, Object> monitorInfoCache =
        new HashMap<Class, Object>();

    private final List<String> filterClassLevel =
        getFilterList("jcarder.classLevel", false);
    private final List<String> filterInclude =
        getFilterList("jcarder.include", true);

    private final static Object sentinelInstanceLevel = new Object();
    private final static Object sentinelIgnore = new Object();

    public static EventListener create(Logger logger, File outputdir)
        throws IOException {
        EventFileWriter eventWriter =
            new EventFileWriter(logger,
                                new File(outputdir, EVENT_DB_FILENAME));
        ContextFileWriter contextWriter =
            new ContextFileWriter(logger,
                                  new File(outputdir, CONTEXTS_DB_FILENAME));
        return new EventListener(logger, eventWriter, contextWriter);
    }

    private List<String> getFilterList(String key, boolean defaultAcceptAll) {
        final List<String> ret = new ArrayList<String>();

        final StringTokenizer tok =
            new StringTokenizer(
                System.getProperty(key, defaultAcceptAll ? " " : ""), ",");
        while (tok.hasMoreTokens()) {
            ret.add(tok.nextToken().trim());
        }
        return ret;
    }

    public EventListener(Logger logger,
                         LockEventListenerIfc lockEventListener,
                         ContextWriterIfc contextWriter) {
        mLogger = logger;
        mLockEventListener = lockEventListener;
        mLockIdGenerator = new LockIdGenerator(mLogger, contextWriter);
        mContextCache = new LockingContextIdCache(mLogger, contextWriter);
        mNumberOfEnteredMonitors =
            new Counter("Entered Monitors", mLogger, 100000);
    }

    public void beforeMonitorEnter(Object monitor, LockingContext context)
        throws Exception {
        mLogger.finest("EventListener.beforeMonitorEnter");
        // Check ignoreFilter and switch to class level if the monitor is
        // matched by classLevelFilter. Results are cached.
        Object classifiedMonitor = checkMonitor(monitor);

        if (classifiedMonitor != sentinelIgnore) {
            mNumberOfEnteredMonitors.increment();
            lockEvent(true, monitor, classifiedMonitor, context);
        }
    }

    public void beforeMonitorExit(Object monitor, LockingContext context)
        throws Exception {
        mLogger.finest("EventListener.beforeMonitorExit");
        // Check ignoreFilter and switch to class level if the monitor is
        // matched by classLevelFilter. Results are cached.
        Object classifiedMonitor = checkMonitor(monitor);

        if (classifiedMonitor != sentinelIgnore) {
            lockEvent(false, monitor, classifiedMonitor, context);
        }
    }

    private Object checkMonitor(Object monitor) {
        // The default behaviour is to treat monitor objects on instance level.
        // This was the old behaviour.
        Object classifiedMonitor = monitor;

        if (monitor != null) {
            final Class cl = monitor.getClass();

            Object firstOccurrence;
            synchronized(this) {
                // Try to use cache.
                firstOccurrence = monitorInfoCache.get(cl);
                if (firstOccurrence == null) {
                    firstOccurrence = checkFilters(monitor, cl);
                }
            }

            // firstOccurence may have three states at this point:
            //
            // 1. sentinelInstanceLevel: (default) The monitor should be
            //    handled for each instance.
            // 2. sentinelIgnore: The monitor is not of interest.
            // 3. Any other instance is the first representation of a monitor
            //    of its class. Class level handling.

            if (firstOccurrence != sentinelInstanceLevel) {
                // By using only one single instance for monitors of a certain
                // class we simulate group level handling.
                classifiedMonitor = firstOccurrence;
            }
        }
        return classifiedMonitor;
    }

    private Object checkFilters(Object monitor, final Class cl) {
        Object firstOccurrence;
        final String clName = cl.getName();

        // Check if include filter matches.
        if (checkIncludeFilter(clName)) {
            // Check if class level filter matches.
            firstOccurrence = checkGroupLevelFilter(monitor, clName);
        } else {
            firstOccurrence = sentinelIgnore;
            mLogger.info("ignoring: " + clName);
        }
        monitorInfoCache.put(cl, firstOccurrence);
        return firstOccurrence;
    }

    private boolean checkIncludeFilter(final String clName) {
        boolean includeFilterMatch = false;
        for (String filter : filterInclude) {
            if (clName.startsWith(filter)) {
                includeFilterMatch = true;
                break;
            }
        }
        return includeFilterMatch;
    }

    private Object checkGroupLevelFilter(Object monitor, final String clName) {
        Object firstOccurrence;
        boolean classLevelFilterMatch = false;
        for (String filter : filterClassLevel) {
            if (clName.startsWith(filter)) {
                classLevelFilterMatch = true;
                break;
            }
        }
        firstOccurrence =
            classLevelFilterMatch ? monitor : sentinelInstanceLevel;
        if (classLevelFilterMatch) {
            mLogger.info("instrumenting on class level: " + clName);
        }
        return firstOccurrence;
    }

    private synchronized void lockEvent(boolean isLock,
                                        Object monitor,
                                        Object classifiedMonitor,
                                        LockingContext context)
        throws Exception {
        int newLockId = mLockIdGenerator.acquireLockId(classifiedMonitor);
        int newContextId = mContextCache.acquireContextId(context);
        Thread performingThread = Thread.currentThread();
        mLockEventListener.onLockEvent(isLock,
                                       newLockId,
                                       newContextId,
                                       performingThread.getId());
    }
}
