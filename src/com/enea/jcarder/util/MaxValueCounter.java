/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

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
