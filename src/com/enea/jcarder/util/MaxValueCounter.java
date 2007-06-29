package com.enea.jcarder.util;

import com.enea.jcarder.util.logging.Logger;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class MaxValueCounter {
    final String mName;
    final Logger mLogger;
    int mValue = 0;
    int mMaxValue = 0;

    public MaxValueCounter(String name, Logger logger) {
        mName = name;
        mLogger = logger;
    }

    public void set(int value) {
        mValue = value;
        if (mValue > mMaxValue) {
            mMaxValue = mValue;
            mLogger.fine("New " + mName + ": " + mMaxValue);
        }
    }

    public String toString() {
        return String.valueOf(mMaxValue);
    }
}
