package com.enea.jcarder.agent.instrument;

// TODO is this config class needed?
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
