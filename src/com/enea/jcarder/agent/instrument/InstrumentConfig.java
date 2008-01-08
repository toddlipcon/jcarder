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

// TODO Is this config class needed?
public final class InstrumentConfig {

    private final boolean mValidateTransfomedClasses = true;
    private boolean mDumpClassFiles;

    public InstrumentConfig() {
        mDumpClassFiles = false;
    }

    public void setDumpClassFiles(boolean dumpClassFiles) {
        mDumpClassFiles = dumpClassFiles;
    }

    public boolean getDumpClassFiles() {
        return mDumpClassFiles;
    }

    public boolean getValidateTransfomedClasses() {
        return mValidateTransfomedClasses;
    }
}
