package com.enea.jcarder.util;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class Counter {
    final int mLogIntervall;
    final String mName;
    final Logger mLogger;
    int mValue = 0;

    public Counter(String name, Logger logger, int logInterval) {
        mName = name;
        mLogger = logger;
        mLogIntervall = logInterval;
    }

    public void increment() {
        mValue++;
        if ((mValue % mLogIntervall) == 0) {
            mLogger.fine(mName + ": " + mValue);
        } else if (mLogger.isFinestEnabled()) {
            mLogger.finest(mName + ": " + mValue);
        }
    }
}
